package com.h5.controller.user;

import com.h5.common.Result;
import com.h5.common.UserContext;
import com.h5.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 余额宝 Controller（已分层，业务逻辑在 WalletService）
 * 所有余额变动通过 BalanceService（乐观锁+流水）
 */
@RestController
@RequestMapping("/wap/yuebao")
public class YuebaoController {

    @Autowired private WalletService walletService;

    @GetMapping("/config")
    public Result<Map<String, Object>> config() {
        return Result.success(walletService.yuebaoConfig());
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> info() {
        return Result.success(walletService.yuebaoInfo(UserContext.getUserId()));
    }

    @GetMapping("/records")
    public Result<Map<String, Object>> records(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(walletService.yuebaoRecords(UserContext.getUserId(), page, pageSize));
    }

    @PostMapping("/transfer-in")
    public Result<Map<String, Object>> transferIn(@RequestBody Map<String, Object> params) {
        BigDecimal amount = params.get("amount") != null
                ? new BigDecimal(params.get("amount").toString()) : null;
        return Result.success(walletService.yuebaoTransferIn(UserContext.getUserId(), amount));
    }

    @PostMapping("/transfer-out")
    public Result<Map<String, Object>> transferOut(@RequestBody Map<String, Object> params) {
        BigDecimal amount = params.get("amount") != null
                ? new BigDecimal(params.get("amount").toString()) : null;
        return Result.success(walletService.yuebaoTransferOut(UserContext.getUserId(), amount));
    }

    @PostMapping("/claim-all")
    public Result<Void> claimAll() {
        return Result.success();
    }
}
