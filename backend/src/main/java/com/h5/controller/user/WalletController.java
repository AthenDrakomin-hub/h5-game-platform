package com.h5.controller.user;

import com.h5.common.Result;
import com.h5.common.UserContext;
import com.h5.entity.UserWallet;
import com.h5.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 钱包地址 Controller（已分层，业务逻辑在 WalletService）
 */
@RestController
@RequestMapping("/wap/user-wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    /** 钱包地址列表 */
    @GetMapping("/list")
    public Result<List<UserWallet>> list() {
        return Result.success(walletService.listWallets(UserContext.getUserId()));
    }

    /** 添加钱包地址 */
    @PostMapping("/add")
    public Result<UserWallet> add(@RequestBody UserWallet wallet) {
        return Result.success(walletService.addWallet(UserContext.getUserId(), wallet));
    }

    /** 删除钱包地址 */
    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody Map<String, Long> params) {
        walletService.deleteWallet(UserContext.getUserId(), params.get("id"));
        return Result.success();
    }
}
