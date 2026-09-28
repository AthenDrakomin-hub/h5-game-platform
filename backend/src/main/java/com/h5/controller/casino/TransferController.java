package com.h5.controller.casino;

import com.h5.common.Result;
import com.h5.common.UserContext;
import com.h5.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 娱乐城转账 Controller（已分层，业务逻辑在 WalletService）
 */
@RestController
@RequestMapping("/wap/transfer")
public class TransferController {

    @Autowired private WalletService walletService;

    /** 各平台钱包余额 */
    @GetMapping("/balances")
    public Result<List<Map<String, Object>>> balances() {
        return Result.success(walletService.transferBalances(UserContext.getUserId()));
    }

    /** 转账记录 */
    @GetMapping("/list")
    public Result<Map<String, Object>> list(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(walletService.transferRecords(UserContext.getUserId(), page, pageSize));
    }
}
