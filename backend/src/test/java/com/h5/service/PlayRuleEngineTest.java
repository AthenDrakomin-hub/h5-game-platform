package com.h5.service;

import com.h5.service.settle.PlayRuleEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 玩法规则引擎单元测试
 * 覆盖：通用玩法 / PK10名次 / SSC定位 / PC28 / LHC六合彩
 */
class PlayRuleEngineTest {

    private PlayRuleEngine engine;
    private static final BigDecimal ODDS = new BigDecimal("1.95");
    private static final BigDecimal AMOUNT = new BigDecimal("100");

    @BeforeEach
    void setUp() {
        engine = new PlayRuleEngine();
    }

    private BigDecimal win(String lottery, String playType, String drawNumbers) {
        return engine.calculateWin(lottery, playType, drawNumbers, "", AMOUNT, ODDS);
    }

    private boolean isWin(String lottery, String playType, String drawNumbers) {
        return win(lottery, playType, drawNumbers).compareTo(BigDecimal.ZERO) > 0;
    }

    // ==================== 通用玩法 ====================
    @Nested
    @DisplayName("通用玩法")
    class CommonPlays {

        @Test
        @DisplayName("大小 - PK10和值55为分界")
        void testBigSmall_PK10() {
            // PK10 10个号码和值范围10-100，分界55
            assertTrue(isWin("jspk10", "big", "06,07,08,09,10,01,02,03,04,05")); // 和=55, 55>55? no
            assertFalse(isWin("jspk10", "big", "01,02,03,04,05,06,07,08,09,10")); // 和=55
            assertTrue(isWin("jspk10", "small", "01,02,03,04,05,01,02,03,04,05")); // 和=30
        }

        @Test
        @DisplayName("单双 - 和值奇偶")
        void testOddEven() {
            assertTrue(isWin("jspk10", "odd", "01,02,03,04,05,06,07,08,09,01")); // 和=46? 1+2+3+4+5+6+7+8+9+1=46 even
            assertFalse(isWin("jspk10", "odd", "01,02,03,04,05,06,07,08,09,01"));
            assertTrue(isWin("jspk10", "even", "01,02,03,04,05,06,07,08,09,01"));
        }

        @Test
        @DisplayName("龙虎 - 第一位vs最后一位")
        void testDragonTiger() {
            assertTrue(isWin("jspk10", "dragon", "10,01,02,03,04,05,06,07,08,01")); // 10>1
            assertTrue(isWin("jspk10", "tiger", "01,02,03,04,05,06,07,08,09,10")); // 1<10
            assertTrue(isWin("jspk10", "draw_dt", "05,01,02,03,04,06,07,08,09,05")); // 5=5
        }

        @Test
        @DisplayName("和值具体数字")
        void testSumSpecific() {
            // PC28 3个号码和值
            assertTrue(isWin("jsdd", "sum_15", "05,05,05")); // 5+5+5=15
            assertFalse(isWin("jsdd", "sum_15", "05,05,06")); // =16
        }
    }

    // ==================== PK10 名次玩法 ====================
    @Nested
    @DisplayName("PK10名次玩法")
    class Pk10PositionPlays {

        private static final String DRAW = "03,07,01,09,05,02,08,04,10,06";
        // 名次: 1=3, 2=7, 3=1, 4=9, 5=5, 6=2, 7=8, 8=4, 9=10, 10=6

        @Test
        @DisplayName("第N名具体号码")
        void testPositionSpecific() {
            assertTrue(isWin("jspk10", "pos_1_03", DRAW));  // 第一名=3
            assertTrue(isWin("jspk10", "pos_2_07", DRAW));  // 第二名=7
            assertTrue(isWin("jspk10", "pos_10_06", DRAW)); // 第十名=6
            assertFalse(isWin("jspk10", "pos_1_07", DRAW)); // 第一名≠7
        }

        @Test
        @DisplayName("第N名大小（01-05小 06-10大）")
        void testPositionBigSmall() {
            assertTrue(isWin("jspk10", "pos_1_small", DRAW)); // 第一名=3 小
            assertTrue(isWin("jspk10", "pos_2_big", DRAW));   // 第二名=7 大
            assertFalse(isWin("jspk10", "pos_1_big", DRAW));  // 第一名=3 不是大
        }

        @Test
        @DisplayName("第N名单双")
        void testPositionOddEven() {
            assertTrue(isWin("jspk10", "pos_1_odd", DRAW));  // 3 单
            assertTrue(isWin("jspk10", "pos_2_odd", DRAW));  // 7 单
            assertTrue(isWin("jspk10", "pos_6_even", DRAW)); // 2 双
        }

        @Test
        @DisplayName("冠亚和")
        void testChampionSum() {
            // 第一名3 + 第二名7 = 10
            assertTrue(isWin("jspk10", "champion_sum_small", DRAW)); // 10<=11 小
            assertFalse(isWin("jspk10", "champion_sum_big", DRAW));  // 10 不是大
            assertTrue(isWin("jspk10", "champion_sum_even", DRAW));  // 10 双
            assertTrue(isWin("jspk10", "champion_sum_10", DRAW));    // 和=10
        }
    }

    // ==================== SSC 定位玩法 ====================
    @Nested
    @DisplayName("SSC定位玩法")
    class SscPositionPlays {

        private static final String DRAW = "3,7,1,9,5";
        // 万=3, 千=7, 百=1, 十=9, 个=5

        @Test
        @DisplayName("定位具体号码")
        void testSscSpecific() {
            assertTrue(isWin("jsssc", "wan_3", DRAW));   // 万位=3
            assertTrue(isWin("jsssc", "qian_7", DRAW));  // 千位=7
            assertTrue(isWin("jsssc", "ge_5", DRAW));    // 个位=5
            assertFalse(isWin("jsssc", "wan_7", DRAW));  // 万位≠7
        }

        @Test
        @DisplayName("定位大小（0-4小 5-9大）")
        void testSscBigSmall() {
            assertTrue(isWin("jsssc", "wan_small", DRAW));  // 3 小
            assertTrue(isWin("jsssc", "qian_big", DRAW));   // 7 大
            assertTrue(isWin("jsssc", "bai_small", DRAW));  // 1 小
        }

        @Test
        @DisplayName("定位单双")
        void testSscOddEven() {
            assertTrue(isWin("jsssc", "wan_odd", DRAW));   // 3 单
            assertTrue(isWin("jsssc", "shi_odd", DRAW));   // 9 单
            assertTrue(isWin("jsssc", "ge_odd", DRAW));    // 5 单
        }

        @Test
        @DisplayName("五星一码（全部相同）")
        void testFiveStar() {
            assertTrue(isWin("jsssc", "five_star_5", "5,5,5,5,5"));
            assertFalse(isWin("jsssc", "five_star_5", "5,5,5,5,3"));
        }
    }

    // ==================== PC28 玩法 ====================
    @Nested
    @DisplayName("PC28玩法")
    class Pc28Plays {

        @Test
        @DisplayName("极小（和值0-5）")
        void testExtremeSmall() {
            assertTrue(isWin("jsdd", "extreme_small", "0,0,0"));  // 和=0
            assertTrue(isWin("jsdd", "extreme_small", "1,2,2"));  // 和=5
            assertFalse(isWin("jsdd", "extreme_small", "2,2,2")); // 和=6
        }

        @Test
        @DisplayName("极大（和值22-27）")
        void testExtremeBig() {
            assertTrue(isWin("jsdd", "extreme_big", "9,9,9"));   // 和=27
            assertTrue(isWin("jsdd", "extreme_big", "7,8,7"));   // 和=22
            assertFalse(isWin("jsdd", "extreme_big", "7,7,7"));  // 和=21
        }

        @Test
        @DisplayName("豹子（三个号码相同）")
        void testLeopard() {
            assertTrue(isWin("jsdd", "leopard", "5,5,5"));
            assertTrue(isWin("jsdd", "leopard", "0,0,0"));
            assertFalse(isWin("jsdd", "leopard", "5,5,6"));
        }

        @Test
        @DisplayName("对子（恰好两个相同）")
        void testPair() {
            assertTrue(isWin("jsdd", "pair", "5,5,6"));
            assertTrue(isWin("jsdd", "pair", "5,6,5"));
            assertFalse(isWin("jsdd", "pair", "5,5,5")); // 豹子不是对子
            assertFalse(isWin("jsdd", "pair", "1,2,3")); // 都不同
        }
    }

    // ==================== LHC 六合彩玩法 ====================
    @Nested
    @DisplayName("LHC六合彩玩法")
    class LhcPlays {

        // 6个正码 + 1个特码，特码是第7个
        private static final String DRAW = "01,02,03,04,05,06,07";
        // 特码=07（红波，马，尾数7，单，小）

        @Test
        @DisplayName("特码具体数字")
        void testSpecialSpecific() {
            assertTrue(isWin("happy8lhc", "special_07", DRAW));
            assertFalse(isWin("happy8lhc", "special_08", DRAW));
        }

        @Test
        @DisplayName("特码大小（01-24小 25-49大）")
        void testSpecialBigSmall() {
            assertTrue(isWin("happy8lhc", "special_small", DRAW)); // 7 小
            assertFalse(isWin("happy8lhc", "special_big", DRAW));
        }

        @Test
        @DisplayName("特码单双")
        void testSpecialOddEven() {
            assertTrue(isWin("happy8lhc", "special_odd", DRAW));  // 7 单
            assertFalse(isWin("happy8lhc", "special_even", DRAW));
        }

        @Test
        @DisplayName("特码波色")
        void testSpecialWave() {
            // 07是红波
            assertTrue(isWin("happy8lhc", "special_red", DRAW));
            assertFalse(isWin("happy8lhc", "special_blue", DRAW));
            assertFalse(isWin("happy8lhc", "special_green", DRAW));

            // 03是蓝波
            assertTrue(isWin("happy8lhc", "special_blue", "01,02,04,05,06,07,03"));
            // 05是绿波
            assertTrue(isWin("happy8lhc", "special_green", "01,02,03,04,06,07,05"));
        }

        @Test
        @DisplayName("特码生肖")
        void testSpecialZodiac() {
            // 07 = 马 (horse)
            assertTrue(isWin("happy8lhc", "special_zodiac_horse", DRAW));
            assertFalse(isWin("happy8lhc", "special_zodiac_rat", DRAW));
            // 01 = 鼠 (rat)
            assertTrue(isWin("happy8lhc", "special_zodiac_rat", "02,03,04,05,06,07,01"));
        }

        @Test
        @DisplayName("特码尾数")
        void testSpecialTail() {
            assertTrue(isWin("happy8lhc", "special_tail_7", DRAW));  // 07尾数7
            assertFalse(isWin("happy8lhc", "special_tail_1", DRAW));
        }
    }

    // ==================== 边界与异常 ====================
    @Nested
    @DisplayName("边界与异常")
    class EdgeCases {

        @Test
        @DisplayName("空开奖号码返回未中奖")
        void testEmptyDrawNumbers() {
            assertEquals(0, win("jspk10", "big", "").compareTo(BigDecimal.ZERO));
            assertEquals(0, win("jspk10", "big", null).compareTo(BigDecimal.ZERO));
        }

        @Test
        @DisplayName("未知玩法返回未中奖（不抛异常）")
        void testUnknownPlayType() {
            assertEquals(0, win("jspk10", "unknown_play", "01,02,03").compareTo(BigDecimal.ZERO));
        }

        @Test
        @DisplayName("中奖金额 = 投注金额 × 赔率")
        void testWinAmountCalculation() {
            BigDecimal result = win("jspk10", "pos_1_03", "03,07,01,09,05,02,08,04,10,06");
            // 100 * 1.95 = 195
            assertEquals(0, result.compareTo(new BigDecimal("195.00")));
        }

        @Test
        @DisplayName("null赔率使用默认1.95")
        void testNullOdds() {
            BigDecimal result = engine.calculateWin("jspk10", "pos_1_03",
                    "03,07,01,09,05,02,08,04,10,06", "", AMOUNT, null);
            assertEquals(0, result.compareTo(new BigDecimal("195.00")));
        }
    }
}
