package com.h5.controller.lottery;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h5.common.BusinessException;
import com.h5.common.ParamValidator;
import com.h5.common.Result;
import com.h5.common.UserContext;
import com.h5.entity.Bet;
import com.h5.entity.User;
import com.h5.mapper.BetMapper;
import com.h5.mapper.UserMapper;
import com.h5.service.BalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/wap/bet")
public class BetController {

    @Autowired private BetMapper betMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private BalanceService balanceService;

    /**
     * 提交投注
     * POST /api/wap/bet/place
     */
    @PostMapping("/place")
    @Transactional
    public Result<Map<String, Object>> place(@RequestBody Map<String, Object> params) {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(401, "用户不存在");

        String lotteryCode = ParamValidator.requireString(params, "lotteryCode", "彩种代码");
        String period = ParamValidator.requireString(params, "period", "期号");
        String playType = ParamValidator.optionalString(params, "playType", "");
        String numbers = ParamValidator.optionalString(params, "numbers", "");
        BigDecimal amount = ParamValidator.requireBigDecimal(params, "amount", "投注金额");
        int count = ParamValidator.optionalInt(params, "count", 1);
        if (count < 1) throw new BusinessException(400, "投注倍数必须大于0");

        BigDecimal totalAmount = amount.multiply(new BigDecimal(count));

        String betNo = "B" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + new Random().nextInt(10000);

        Bet bet = new Bet();
        bet.setBetNo(betNo);
        bet.setUserId(userId);
        bet.setLotteryCode(lotteryCode);
        bet.setPeriod(period);
        bet.setPlayType(playType);
        bet.setNumbers(numbers);
        bet.setAmount(totalAmount);
        bet.setOdds(new BigDecimal("1.95"));
        bet.setWinAmount(BigDecimal.ZERO);
        bet.setStatus("pending");
        betMapper.insert(bet);

        // 使用 BalanceService 安全扣减余额（乐观锁 + 自动写流水）
        User updated = balanceService.deductBalance(
                userId, totalAmount, "bet",
                bet.getId(), betNo,
                lotteryCode + " 投注 " + playType + " " + numbers
        );

        Map<String, Object> data = new HashMap<>();
        data.put("betNo", betNo);
        data.put("amount", totalAmount);
        data.put("balance", updated.getBalance());
        data.put("status", "pending");
        return Result.success(data);
    }

    /**
     * 投注记录
     * GET /api/wap/bet/list?page=1&pageSize=10
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String lotteryCode,
            @RequestParam(required = false) String status) {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<Bet> wrapper = new LambdaQueryWrapper<Bet>()
                .eq(Bet::getUserId, userId)
                .orderByDesc(Bet::getCreateTime);
        if (lotteryCode != null && !lotteryCode.isEmpty()) {
            wrapper.eq(Bet::getLotteryCode, lotteryCode);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Bet::getStatus, status);
        }
        Page<Bet> pageResult = betMapper.selectPage(new Page<>(page, pageSize), wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", page);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    /**
     * 投注详情
     * GET /api/wap/bet/detail/{betNo}
     */
    @GetMapping("/detail/{betNo}")
    public Result<Bet> detail(@PathVariable String betNo) {
        Long userId = UserContext.getUserId();
        Bet bet = betMapper.selectOne(
                new LambdaQueryWrapper<Bet>()
                        .eq(Bet::getBetNo, betNo)
                        .eq(Bet::getUserId, userId)
        );
        return Result.success(bet);
    }
}
