package com.h5.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h5.common.BusinessException;
import com.h5.entity.Promotion;
import com.h5.entity.User;
import com.h5.mapper.PromotionMapper;
import com.h5.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 活动服务（活动列表/签到/转盘/任务）
 */
@Service
public class PromoService {

    @Autowired private PromotionMapper promotionMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private BalanceService balanceService;

    /** 签到记录（内存存储，生产应换 Redis/DB） */
    private final Map<Long, Set<LocalDate>> signInRecords = new HashMap<>();

    /**
     * 活动列表
     */
    public Map<String, Object> getPromotionList(String category, int page, int pageSize) {
        LambdaQueryWrapper<Promotion> wrapper = new LambdaQueryWrapper<Promotion>()
                .eq(Promotion::getStatus, 1)
                .orderByAsc(Promotion::getSort);
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Promotion::getCategory, category);
        }
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Promotion> pageResult =
                promotionMapper.selectPage(
                        new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize), wrapper);
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", page);
        data.put("pageSize", pageSize);
        return data;
    }

    /**
     * 活动详情
     */
    public Promotion getPromotionDetail(Long id) {
        Promotion promo = promotionMapper.selectById(id);
        if (promo == null) throw new BusinessException("活动不存在");
        return promo;
    }

    /**
     * 活动分类
     */
    public List<Map<String, Object>> getCategories() {
        List<Promotion> all = promotionMapper.selectList(
                new LambdaQueryWrapper<Promotion>().eq(Promotion::getStatus, 1)
        );
        Map<String, String> catMap = new LinkedHashMap<>();
        for (Promotion p : all) {
            if (p.getCategory() != null && !catMap.containsKey(p.getCategory())) {
                catMap.put(p.getCategory(), p.getCategoryName());
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, String> e : catMap.entrySet()) {
            Map<String, Object> cat = new HashMap<>();
            cat.put("code", e.getKey());
            cat.put("name", e.getValue());
            result.add(cat);
        }
        return result;
    }

    /**
     * 每日签到
     */
    @Transactional
    public Map<String, Object> dailySignIn(Long userId) {
        LocalDate today = LocalDate.now();
        Set<LocalDate> records = signInRecords.computeIfAbsent(userId, k -> new HashSet<>());

        if (records.contains(today)) {
            throw new BusinessException("今日已签到");
        }
        records.add(today);

        // 连续签到天数
        int continuousDays = calculateContinuousDays(userId);
        // 签到奖励（连续天数递增，第7天重置）
        BigDecimal reward = new BigDecimal("10").multiply(new BigDecimal(continuousDays));
        reward = reward.min(new BigDecimal("100")); // 上限100

        balanceService.addBalance(userId, reward, "bonus", null, null,
                "每日签到奖励(连续" + continuousDays + "天)");

        Map<String, Object> data = new HashMap<>();
        data.put("reward", reward);
        data.put("continuousDays", continuousDays);
        data.put("signed", true);
        return data;
    }

    /**
     * 签到状态
     */
    public Map<String, Object> getSignInStatus(Long userId) {
        LocalDate today = LocalDate.now();
        Set<LocalDate> records = signInRecords.getOrDefault(userId, Collections.emptySet());
        Map<String, Object> data = new HashMap<>();
        data.put("todaySigned", records.contains(today));
        data.put("continuousDays", calculateContinuousDays(userId));
        return data;
    }

    /**
     * 幸运转盘（随机奖励）
     */
    @Transactional
    public Map<String, Object> luckyWheel(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(401, "用户不存在");

        // 转盘奖励配置（概率）
        List<Object[]> prizes = Arrays.asList(
                new Object[]{"谢谢参与", BigDecimal.ZERO, 30},
                new Object[]{"5元彩金", new BigDecimal("5"), 25},
                new Object[]{"10元彩金", new BigDecimal("10"), 20},
                new Object[]{"20元彩金", new BigDecimal("20"), 15},
                new Object[]{"50元彩金", new BigDecimal("50"), 8},
                new Object[]{"100元彩金", new BigDecimal("100"), 2}
        );

        int rand = new Random().nextInt(100);
        int cumulative = 0;
        String prizeName = "谢谢参与";
        BigDecimal prizeAmount = BigDecimal.ZERO;
        for (Object[] prize : prizes) {
            cumulative += (int) prize[2];
            if (rand < cumulative) {
                prizeName = (String) prize[0];
                prizeAmount = (BigDecimal) prize[1];
                break;
            }
        }

        if (prizeAmount.compareTo(BigDecimal.ZERO) > 0) {
            balanceService.addBalance(userId, prizeAmount, "bonus", null, null,
                    "幸运转盘-" + prizeName);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("prize", prizeName);
        data.put("amount", prizeAmount);
        return data;
    }

    private int calculateContinuousDays(Long userId) {
        Set<LocalDate> records = signInRecords.getOrDefault(userId, Collections.emptySet());
        int days = 0;
        LocalDate date = LocalDate.now();
        while (records.contains(date)) {
            days++;
            date = date.minusDays(1);
        }
        return days;
    }
}
