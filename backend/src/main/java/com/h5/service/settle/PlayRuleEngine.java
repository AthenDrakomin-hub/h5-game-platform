package com.h5.service.settle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 玩法规则引擎（全玩法版）
 *
 * 支持彩种：PK10 / SSC / PC28 / LHC(六合彩) / 飞艇 / 运动会
 *
 * 玩法编码规范：
 * ===== 通用 =====
 *   big / small              大小（和值）
 *   odd / even               单双（和值）
 *   dragon / tiger / draw_dt 龙虎（第一位 vs 最后一位）
 *   sum_{n}                  和值具体数字
 *   sum_big / sum_small      和值大小
 *   sum_odd / sum_even       和值单双
 *
 * ===== PK10/飞艇（10个号码 01-10）=====
 *   pos_{1-10}_{01-10}      第N名具体号码（如 pos_1_03 = 第一名是3号）
 *   pos_{1-10}_big/small    第N名大小（01-05小 06-10大）
 *   pos_{1-10}_odd/even     第N名单双
 *   champion_sum             冠亚和（第一名+第二名）
 *   champion_sum_big/small   冠亚和大小（≥12大 ≤11小）
 *   champion_sum_odd/even    冠亚和单双
 *
 * ===== SSC（5个号码 0-9，万/千/百/十/个）=====
 *   wan_{0-9} / qian_{0-9} / bai_{0-9} / shi_{0-9} / ge_{0-9}  定位具体号
 *   wan_big/small/odd/even   万位大小单双（0-4小 5-9大）
 *   qian_big/small/odd/even  千位...
 *   bai_big/small/odd/even   百位...
 *   shi_big/small/odd/even   十位...
 *   ge_big/small/odd/even    个位...
 *   five_star_{n}            五星一码（全部5位相同）
 *
 * ===== PC28（3个号码 0-9，和值0-27）=====
 *   extreme_small            极小（和值0-5）
 *   extreme_big              极大（和值22-27）
 *   leopard                  豹子（三个号码相同）
 *   pair                     对子（恰好两个号码相同）
 *   sum_{0-27}               和值具体数字
 *
 * ===== LHC 六合彩（6个正码 + 1个特码 01-49）=====
 *   special_{01-49}          特码具体数字
 *   special_big/small        特码大小（01-24小 25-49大）
 *   special_odd/even         特码单双
 *   special_red/blue/green   特码波色
 *   special_zodiac_{name}    特码生肖（rat/ox/tiger/rabbit/dragon/snake/horse/goat/monkey/rooster/dog/pig）
 *   special_tail_{0-9}       特码尾数
 */
@Component
public class PlayRuleEngine {

    private static final Logger log = LoggerFactory.getLogger(PlayRuleEngine.class);

    // 六合彩波色映射
    private static final Set<Integer> RED_WAVE = Set.of(
            1,2,7,8,12,13,18,19,23,24,29,30,34,35,40,45,46);
    private static final Set<Integer> BLUE_WAVE = Set.of(
            3,4,9,10,14,15,20,25,26,31,36,37,41,42,47,48);
    // 绿波 = 其余 (5,6,11,16,17,21,22,27,28,32,33,38,39,43,44,49)

    // 六合彩生肖映射（按固定顺位，实际应按年份调整）
    private static final Map<String, Set<Integer>> ZODIAC_MAP = new HashMap<>();
    static {
        int[][] zodiacRanges = {
                {1,13,25,37,49},  // 鼠
                {2,14,26,38},      // 牛
                {3,15,27,39},      // 虎
                {4,16,28,40},      // 兔
                {5,17,29,41},      // 龙
                {6,18,30,42},      // 蛇
                {7,19,31,43},      // 马
                {8,20,32,44},      // 羊
                {9,21,33,45},      // 猴
                {10,22,34,46},     // 鸡
                {11,23,35,47},     // 狗
                {12,24,36,48},     // 猪
        };
        String[] names = {"rat","ox","tiger","rabbit","dragon","snake",
                "horse","goat","monkey","rooster","dog","pig"};
        for (int i = 0; i < names.length; i++) {
            Set<Integer> set = new HashSet<>();
            for (int n : zodiacRanges[i]) set.add(n);
            ZODIAC_MAP.put(names[i], set);
        }
    }

    /**
     * 计算中奖金额
     *
     * @return 中奖金额（0 表示未中）
     */
    public BigDecimal calculateWin(String lotteryCode, String playType,
                                    String drawNumbers, String betNumbers,
                                    BigDecimal amount, BigDecimal odds) {
        if (playType == null || playType.isEmpty()) {
            return BigDecimal.ZERO;
        }

        try {
            List<Integer> nums = parseNumbers(drawNumbers);
            if (nums.isEmpty()) {
                return BigDecimal.ZERO;
            }

            boolean win = evaluatePlay(lotteryCode, playType, nums, betNumbers);

            if (win) {
                return amount.multiply(odds != null ? odds : new BigDecimal("1.95"));
            }
            return BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("玩法计算异常, lottery={}, playType={}, error={}",
                    lotteryCode, playType, e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * 核心玩法判定
     */
    private boolean evaluatePlay(String lotteryCode, String playType,
                                  List<Integer> nums, String betNumbers) {
        String pt = playType.toLowerCase().trim();
        int sum = nums.stream().mapToInt(Integer::intValue).sum();
        int first = nums.get(0);
        int last = nums.get(nums.size() - 1);

        // ===== 通用玩法 =====
        switch (pt) {
            case "big": return sum > getSumMid(lotteryCode);
            case "small": return sum < getSumMid(lotteryCode);
            case "odd": return sum % 2 == 1;
            case "even": return sum % 2 == 0;
            case "dragon": return first > last;
            case "tiger": return first < last;
            case "draw_dt": return first == last;
            case "sum_big": return sum >= getSumBigThreshold(lotteryCode);
            case "sum_small": return sum < getSumBigThreshold(lotteryCode);
            case "sum_odd": return sum % 2 == 1;
            case "sum_even": return sum % 2 == 0;
        }

        // ===== 和值具体数字 =====
        if (pt.startsWith("sum_")) {
            try {
                int target = Integer.parseInt(pt.substring(4));
                return sum == target;
            } catch (NumberFormatException ignored) {}
        }

        // ===== PC28 专用 =====
        if ("jsdd".equals(lotteryCode)) {
            switch (pt) {
                case "extreme_small": return sum <= 5;
                case "extreme_big": return sum >= 22;
                case "leopard":
                    return nums.size() >= 3 && nums.get(0).equals(nums.get(1)) && nums.get(1).equals(nums.get(2));
                case "pair":
                    if (nums.size() < 3) return false;
                    boolean a = nums.get(0).equals(nums.get(1));
                    boolean b = nums.get(1).equals(nums.get(2));
                    boolean c = nums.get(0).equals(nums.get(2));
                    return (a || b || c) && !(a && b && c); // 恰好两个相同
            }
        }

        // ===== PK10/飞艇 名次玩法（10个号码）=====
        if (pt.startsWith("pos_")) {
            return evaluatePositionPlay(pt, nums);
        }

        // ===== PK10 冠亚和 =====
        if (pt.startsWith("champion_sum")) {
            if (nums.size() < 2) return false;
            int championSum = nums.get(0) + nums.get(1);
            switch (pt) {
                case "champion_sum": return true; // 冠亚和本身（一般不单独下注，返回true配合特殊赔率）
                case "champion_sum_big": return championSum >= 12;
                case "champion_sum_small": return championSum <= 11;
                case "champion_sum_odd": return championSum % 2 == 1;
                case "champion_sum_even": return championSum % 2 == 0;
            }
            // champion_sum_{n} 具体和值
            try {
                int target = Integer.parseInt(pt.substring("champion_sum_".length()));
                return championSum == target;
            } catch (NumberFormatException ignored) {}
        }

        // ===== SSC 定位玩法（5个号码：万/千/百/十/个）=====
        if ("jsssc".equals(lotteryCode) && nums.size() >= 5) {
            if (pt.startsWith("five_star_")) {
                try {
                    int target = Integer.parseInt(pt.substring(10));
                    return nums.stream().allMatch(n -> n == target);
                } catch (NumberFormatException ignored) {}
            }
            return evaluateSscPositionPlay(pt, nums);
        }

        // ===== LHC 六合彩特码玩法 =====
        if ("happy8lhc".equals(lotteryCode)) {
            int special = extractLhcSpecial(nums);
            if (special > 0) {
                return evaluateLhcPlay(pt, special);
            }
        }

        // ===== 未知玩法 =====
        log.warn("未知玩法类型: {} (lottery={})", playType, lotteryCode);
        return false;
    }

    /**
     * PK10 名次玩法判定
     * pos_{1-10}_{01-10}    第N名具体号码
     * pos_{1-10}_big/small   第N名大小（01-05小 06-10大）
     * pos_{1-10}_odd/even    第N名单双
     */
    private boolean evaluatePositionPlay(String pt, List<Integer> nums) {
        String[] parts = pt.split("_");
        if (parts.length < 3) return false;
        try {
            int pos = Integer.parseInt(parts[1]);
            if (pos < 1 || pos > nums.size()) return false;
            int value = nums.get(pos - 1);
            String condition = parts[2];

            switch (condition) {
                case "big": return value >= 6;
                case "small": return value <= 5;
                case "odd": return value % 2 == 1;
                case "even": return value % 2 == 0;
                default:
                    // 具体号码
                    try {
                        int target = Integer.parseInt(condition);
                        return value == target;
                    } catch (NumberFormatException e) {
                        return false;
                    }
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * SSC 定位玩法判定
     * wan/qian/bai/shi/ge + big/small/odd/even + 具体数字
     * 万位=index0, 千位=index1, 百位=index2, 十位=index3, 个位=index4
     */
    private boolean evaluateSscPositionPlay(String pt, List<Integer> nums) {
        Map<String, Integer> positionIndex = Map.of(
                "wan", 0, "qian", 1, "bai", 2, "shi", 3, "ge", 4
        );

        for (Map.Entry<String, Integer> entry : positionIndex.entrySet()) {
            String prefix = entry.getKey() + "_";
            if (pt.startsWith(prefix)) {
                int idx = entry.getValue();
                if (idx >= nums.size()) return false;
                int value = nums.get(idx);
                String condition = pt.substring(prefix.length());

                switch (condition) {
                    case "big": return value >= 5;
                    case "small": return value <= 4;
                    case "odd": return value % 2 == 1;
                    case "even": return value % 2 == 0;
                    default:
                        try {
                            return value == Integer.parseInt(condition);
                        } catch (NumberFormatException e) {
                            return false;
                        }
                }
            }
        }
        return false;
    }

    /**
     * LHC 特码玩法判定
     */
    private boolean evaluateLhcPlay(String pt, int special) {
        switch (pt) {
            case "special_big": return special >= 25;
            case "special_small": return special <= 24;
            case "special_odd": return special % 2 == 1;
            case "special_even": return special % 2 == 0;
            case "special_red": return RED_WAVE.contains(special);
            case "special_blue": return BLUE_WAVE.contains(special);
            case "special_green": return !RED_WAVE.contains(special) && !BLUE_WAVE.contains(special);
        }

        // 特码具体数字
        if (pt.startsWith("special_")) {
            String rest = pt.substring(8);
            try {
                return special == Integer.parseInt(rest);
            } catch (NumberFormatException ignored) {}
        }

        // 特码生肖
        if (pt.startsWith("special_zodiac_")) {
            String zodiac = pt.substring("special_zodiac_".length());
            Set<Integer> zodiacSet = ZODIAC_MAP.get(zodiac);
            return zodiacSet != null && zodiacSet.contains(special);
        }

        // 特码尾数
        if (pt.startsWith("special_tail_")) {
            try {
                int tail = Integer.parseInt(pt.substring("special_tail_".length()));
                return special % 10 == tail;
            } catch (NumberFormatException ignored) {}
        }

        return false;
    }

    /**
     * 从六合彩开奖号码中提取特码
     * 格式: "01,02,03,04,05,06,+07" 或 "01,02,03,04,05,06,07"
     * 特码是最后一个（带+号或第7个）
     */
    private int extractLhcSpecial(List<Integer> nums) {
        if (nums.size() >= 7) {
            return nums.get(6); // 第7个是特码
        }
        if (nums.size() >= 1) {
            return nums.get(nums.size() - 1); // 退化：取最后一个
        }
        return -1;
    }

    /**
     * 大小分界值
     */
    private int getSumMid(String lotteryCode) {
        return switch (lotteryCode) {
            case "jspk10", "jsft" -> 55;
            case "jsssc" -> 22;
            case "jsdd" -> 14;
            case "happy8lhc" -> 150;
            default -> 50;
        };
    }

    /**
     * 和值大小阈值（sum_big/sum_small）
     */
    private int getSumBigThreshold(String lotteryCode) {
        return switch (lotteryCode) {
            case "jsdd" -> 14;  // PC28: 14-27大
            case "jsssc" -> 23; // SSC: 23-45大
            case "jspk10", "jsft" -> 56; // PK10: 56-110大
            default -> getSumMid(lotteryCode) + 1;
        };
    }

    /**
     * 解析开奖号码字符串
     */
    private List<Integer> parseNumbers(String numbers) {
        if (numbers == null || numbers.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(numbers.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.startsWith("+") ? s.substring(1) : s)
                .map(s -> {
                    try { return Integer.parseInt(s); }
                    catch (NumberFormatException e) { return null; }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
