package com.h5.service.draw;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h5.entity.DrawResult;
import com.h5.entity.Lottery;
import com.h5.mapper.DrawResultMapper;
import com.h5.mapper.LotteryMapper;
import com.h5.service.settle.LotterySettleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 开奖调度服务
 *
 * 职责：
 * 1. 定时（每10秒）检查所有启用彩种是否到达开奖时间
 * 2. 调用 DrawDataProvider 获取开奖数据（优先第三方API，失败fallback随机）
 * 3. 幂等写入 draw_results 表（同一彩种+期号不重复）
 * 4. 触发 LotterySettleService 结算该期投注
 *
 * 开奖时间判断：
 *   基于 lotteries.draw_interval（秒），从每小时整点开始计算期号
 *   期号格式：yyyyMMddHHmm（取该期开奖时间的分钟）
 */
@Service
public class DrawService {

    private static final Logger log = LoggerFactory.getLogger(DrawService.class);
    private static final DateTimeFormatter PERIOD_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    @Autowired
    private LotteryMapper lotteryMapper;

    @Autowired
    private DrawResultMapper drawResultMapper;

    @Autowired(required = false)
    private MarkSix6Provider markSix6Provider;

    @Autowired
    private RandomDrawProvider randomDrawProvider;

    @Autowired
    private LotterySettleService settleService;

    @Value("${draw.scheduling.enabled:true}")
    private boolean schedulingEnabled;

    /** 记录每个彩种最后开奖期号，防止重复开奖 */
    private final Map<String, String> lastPeriodMap = new ConcurrentHashMap<>();

    /**
     * 定时开奖任务：每10秒执行一次
     */
    @Scheduled(fixedDelay = 10000)
    public void scheduledDraw() {
        if (!schedulingEnabled) {
            return;
        }
        try {
            checkAndDraw();
        } catch (Exception e) {
            log.error("定时开奖任务异常", e);
        }
    }

    /**
     * 检查所有启用彩种并执行开奖
     */
    public void checkAndDraw() {
        List<Lottery> lotteries = lotteryMapper.selectList(
                new LambdaQueryWrapper<Lottery>().eq(Lottery::getStatus, 1)
        );

        for (Lottery lottery : lotteries) {
            try {
                String currentPeriod = calculateCurrentPeriod(lottery);
                if (currentPeriod == null) continue;

                // 检查是否已开奖（内存标记 + 数据库双重校验）
                String lastPeriod = lastPeriodMap.get(lottery.getCode());
                if (currentPeriod.equals(lastPeriod)) {
                    continue;
                }

                // 数据库幂等检查
                DrawResult existing = drawResultMapper.selectOne(
                        new LambdaQueryWrapper<DrawResult>()
                                .eq(DrawResult::getLotteryCode, lottery.getCode())
                                .eq(DrawResult::getPeriod, currentPeriod)
                );
                if (existing != null) {
                    lastPeriodMap.put(lottery.getCode(), currentPeriod);
                    continue;
                }

                // 执行开奖
                DrawResult result = fetchDrawResult(lottery.getCode());
                if (result == null) {
                    log.warn("彩种 {} 获取开奖数据失败，跳过本期", lottery.getCode());
                    continue;
                }
                result.setPeriod(currentPeriod); // 强制使用计算出的期号

                // 写入数据库
                drawResultMapper.insert(result);
                lastPeriodMap.put(lottery.getCode(), currentPeriod);
                log.info("开奖: lottery={}, period={}, numbers={}",
                        lottery.getCode(), currentPeriod, result.getNumbers());

                // 触发结算（异步或同步，此处同步）
                try {
                    settleService.settlePeriod(result);
                } catch (Exception e) {
                    log.error("结算失败, lottery={}, period={}, error={}",
                            lottery.getCode(), currentPeriod, e.getMessage(), e);
                }

            } catch (Exception e) {
                log.error("彩种 {} 开奖异常: {}", lottery.getCode(), e.getMessage(), e);
            }
        }
    }

    /**
     * 获取开奖结果（优先第三方API，失败fallback随机）
     */
    private DrawResult fetchDrawResult(String lotteryCode) {
        // 1. 尝试第三方 API
        if (markSix6Provider != null && markSix6Provider.isEnabled()) {
            try {
                DrawResult result = markSix6Provider.getLatestDraw(lotteryCode);
                if (result != null) {
                    log.debug("使用 MarkSix6 开奖数据: {}", lotteryCode);
                    return result;
                }
            } catch (Exception e) {
                log.warn("MarkSix6 获取失败，使用随机开奖: {}", e.getMessage());
            }
        }

        // 2. fallback 到随机开奖
        return randomDrawProvider.getLatestDraw(lotteryCode);
    }

    /**
     * 计算当前应该开奖的期号
     * 基于 draw_interval 从每小时整点开始计算
     *
     * @return 当前期号，如果还在封盘期内返回 null
     */
    private String calculateCurrentPeriod(Lottery lottery) {
        int interval = lottery.getDrawInterval() != null ? lottery.getDrawInterval() : 60;
        int closeTime = lottery.getCloseTime() != null ? lottery.getCloseTime() : 15;

        LocalDateTime now = LocalDateTime.now();
        // 取当前小时的整点
        LocalDateTime hourStart = now.withMinute(0).withSecond(0).withNano(0);
        // 计算从整点到现在的秒数
        long secondsFromHour = java.time.Duration.between(hourStart, now).getSeconds();
        // 计算当前期索引
        long periodIndex = secondsFromHour / interval;
        // 本期开奖时间
        LocalDateTime drawTime = hourStart.plusSeconds(periodIndex * interval);

        // 封盘期：距离下期开奖不足 closeTime 秒时，当前期已封盘但可能还未开奖
        // 这里简化：只要过了开奖时间就可以开奖
        if (now.isBefore(drawTime)) {
            return null; // 还没到开奖时间
        }

        return drawTime.format(PERIOD_FORMAT);
    }

    /**
     * 手动触发某彩种开奖（管理端调用）
     */
    public DrawResult manualDraw(String lotteryCode) {
        Lottery lottery = lotteryMapper.selectOne(
                new LambdaQueryWrapper<Lottery>().eq(Lottery::getCode, lotteryCode)
        );
        if (lottery == null) {
            throw new IllegalArgumentException("彩种不存在: " + lotteryCode);
        }

        String period = LocalDateTime.now().format(PERIOD_FORMAT);
        DrawResult result = fetchDrawResult(lotteryCode);
        result.setLotteryCode(lotteryCode);
        result.setPeriod(period);
        result.setStatus(1);

        drawResultMapper.insert(result);
        lastPeriodMap.put(lotteryCode, period);

        // 触发结算
        settleService.settlePeriod(result);

        return result;
    }
}
