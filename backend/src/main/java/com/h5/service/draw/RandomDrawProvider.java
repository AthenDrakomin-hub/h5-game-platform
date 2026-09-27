package com.h5.service.draw;

import com.h5.entity.DrawResult;
import com.h5.entity.Lottery;
import com.h5.mapper.LotteryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 内置随机开奖提供者（fallback）
 * 当第三方 API 不可用或未配置时使用，按 lotteries 表的开奖间隔自动生成随机号码
 *
 * 各彩种号码生成规则：
 *   PK10/飞艇: 10 个 01-10 不重复号码
 *   SSC: 5 个 0-9 数字
 *   PC28: 3 个 0-9 数字（和值 0-27）
 *   LHC: 6 个 01-49 不重复号码 + 1 个特码
 *   运动会: 6 个 01-06 不重复号码
 */
@Component
public class RandomDrawProvider implements DrawDataProvider {

    private static final Logger log = LoggerFactory.getLogger(RandomDrawProvider.class);
    private static final DateTimeFormatter PERIOD_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private final Random random = new Random();

    @Autowired
    private LotteryMapper lotteryMapper;

    @Override
    public String getName() {
        return "random";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public DrawResult getLatestDraw(String lotteryCode) {
        Lottery lottery = lotteryMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Lottery>()
                        .eq(Lottery::getCode, lotteryCode)
        );
        if (lottery == null) {
            return null;
        }
        return generateDraw(lottery);
    }

    @Override
    public List<DrawResult> getLatestDraws(List<String> lotteryCodes) {
        List<DrawResult> results = new ArrayList<>();
        for (String code : lotteryCodes) {
            DrawResult r = getLatestDraw(code);
            if (r != null) results.add(r);
        }
        return results;
    }

    @Override
    public List<DrawResult> getHistoryDraws(String lotteryCode, int limit) {
        // 随机模式不提供历史记录
        return new ArrayList<>();
    }

    /**
     * 根据彩种生成随机开奖结果
     */
    private DrawResult generateDraw(Lottery lottery) {
        String code = lottery.getCode();
        String numbers = switch (code) {
            case "jspk10", "jsft" -> generateUniqueNumbers(10, 1, 10);
            case "jsssc" -> generateDigits(5);
            case "jsdd" -> generateDigits(3);
            case "happy8lhc" -> generateLhcNumbers();
            case "jsydh" -> generateUniqueNumbers(6, 1, 6);
            default -> generateDigits(5);
        };

        DrawResult result = new DrawResult();
        result.setLotteryCode(code);
        result.setPeriod(LocalDateTime.now().format(PERIOD_FORMAT));
        result.setNumbers(numbers);
        result.setDrawTime(LocalDateTime.now());
        result.setStatus(1);
        return result;
    }

    /** 生成 count 个 [min,max] 不重复号码，格式 01,02,... */
    private String generateUniqueNumbers(int count, int min, int max) {
        List<Integer> pool = new ArrayList<>();
        for (int i = min; i <= max; i++) pool.add(i);
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < count && !pool.isEmpty(); i++) {
            int idx = random.nextInt(pool.size());
            result.add(pool.remove(idx));
        }
        return result.stream()
                .map(n -> String.format("%02d", n))
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }

    /** 生成 count 个 0-9 数字 */
    private String generateDigits(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(",");
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /** 六合彩：6 个 01-49 不重复 + 特码 */
    private String generateLhcNumbers() {
        String main = generateUniqueNumbers(6, 1, 49);
        int special = random.nextInt(49) + 1;
        return main + ",+" + String.format("%02d", special);
    }
}
