package com.h5.service.settle;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h5.entity.Bet;
import com.h5.entity.DrawResult;
import com.h5.mapper.BetMapper;
import com.h5.service.BalanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 彩票结算引擎
 * 开奖后扫描该期所有待结算投注，按玩法规则计算中奖并发放奖金
 *
 * 结算流程：
 * 1. 查询指定彩种+期号的所有 pending 投注
 * 2. 逐注调用 PlayRuleEngine 计算中奖金额
 * 3. 中奖：BalanceService.addBalance 发放奖金 + bets.status=win + win_amount
 * 4. 未中：bets.status=lose
 * 5. 整期结算在事务内，单注失败不影响其他注（catch 单注异常）
 */
@Service
public class LotterySettleService {

    private static final Logger log = LoggerFactory.getLogger(LotterySettleService.class);

    @Autowired
    private BetMapper betMapper;

    @Autowired
    private PlayRuleEngine playRuleEngine;

    @Autowired
    private BalanceService balanceService;

    /**
     * 结算某一期
     *
     * @param drawResult 开奖结果
     * @return 结算统计 {total, win, lose, totalWinAmount}
     */
    @Transactional
    public java.util.Map<String, Object> settlePeriod(DrawResult drawResult) {
        String lotteryCode = drawResult.getLotteryCode();
        String period = drawResult.getPeriod();
        String numbers = drawResult.getNumbers();

        log.info("开始结算: lottery={}, period={}, numbers={}", lotteryCode, period, numbers);

        // 查询该期所有待结算投注
        List<Bet> pendingBets = betMapper.selectList(
                new LambdaQueryWrapper<Bet>()
                        .eq(Bet::getLotteryCode, lotteryCode)
                        .eq(Bet::getPeriod, period)
                        .eq(Bet::getStatus, "pending")
        );

        int total = pendingBets.size();
        int winCount = 0;
        int loseCount = 0;
        BigDecimal totalWinAmount = BigDecimal.ZERO;

        for (Bet bet : pendingBets) {
            try {
                BigDecimal winAmount = playRuleEngine.calculateWin(
                        lotteryCode,
                        bet.getPlayType(),
                        numbers,
                        bet.getNumbers(),
                        bet.getAmount(),
                        bet.getOdds()
                );

                if (winAmount != null && winAmount.compareTo(BigDecimal.ZERO) > 0) {
                    // 中奖
                    settleWinBet(bet, winAmount);
                    winCount++;
                    totalWinAmount = totalWinAmount.add(winAmount);
                } else {
                    // 未中
                    settleLoseBet(bet);
                    loseCount++;
                }
            } catch (Exception e) {
                log.error("结算投注失败, betNo={}, error={}", bet.getBetNo(), e.getMessage(), e);
                // 单注失败不影响其他注，保持 pending 可手动处理
            }
        }

        log.info("结算完成: lottery={}, period={}, total={}, win={}, lose={}, totalWinAmount={}",
                lotteryCode, period, total, winCount, loseCount, totalWinAmount);

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("lotteryCode", lotteryCode);
        result.put("period", period);
        result.put("total", total);
        result.put("win", winCount);
        result.put("lose", loseCount);
        result.put("totalWinAmount", totalWinAmount);
        return result;
    }

    /**
     * 结算中奖投注
     */
    private void settleWinBet(Bet bet, BigDecimal winAmount) {
        // 更新投注状态
        bet.setStatus("win");
        bet.setWinAmount(winAmount);
        betMapper.updateById(bet);

        // 发放奖金
        balanceService.addBalance(
                bet.getUserId(),
                winAmount,
                "win",
                bet.getId(),
                bet.getBetNo(),
                bet.getLotteryCode() + " 中奖 " + bet.getPlayType()
        );
    }

    /**
     * 结算未中投注
     */
    private void settleLoseBet(Bet bet) {
        bet.setStatus("lose");
        bet.setWinAmount(BigDecimal.ZERO);
        betMapper.updateById(bet);
    }

    /**
     * 检查某期是否已结算（是否还有 pending 投注）
     */
    public boolean isPeriodSettled(String lotteryCode, String period) {
        Long count = betMapper.selectCount(
                new LambdaQueryWrapper<Bet>()
                        .eq(Bet::getLotteryCode, lotteryCode)
                        .eq(Bet::getPeriod, period)
                        .eq(Bet::getStatus, "pending")
        );
        return count == 0;
    }
}
