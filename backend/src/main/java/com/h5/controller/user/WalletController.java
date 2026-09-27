package com.h5.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h5.common.BusinessException;
import com.h5.common.Result;
import com.h5.common.UserContext;
import com.h5.entity.UserWallet;
import com.h5.mapper.UserWalletMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wap/user-wallet")
public class WalletController {

    @Autowired
    private UserWalletMapper walletMapper;

    /**
     * 钱包地址列表
     * GET /api/wap/user-wallet/list
     */
    @GetMapping("/list")
    public Result<List<UserWallet>> list() {
        Long userId = UserContext.getUserId();
        List<UserWallet> list = walletMapper.selectList(
                new LambdaQueryWrapper<UserWallet>()
                        .eq(UserWallet::getUserId, userId)
                        .eq(UserWallet::getStatus, 1)
                        .orderByDesc(UserWallet::getIsDefault)
                        .orderByDesc(UserWallet::getCreateTime)
        );
        return Result.success(list);
    }

    /**
     * 添加钱包地址
     * POST /api/wap/user-wallet/add
     */
    @PostMapping("/add")
    @Transactional
    public Result<UserWallet> add(@RequestBody UserWallet wallet) {
        Long userId = UserContext.getUserId();
        if (wallet.getAddress() == null || wallet.getChain() == null) {
            throw new BusinessException("链类型和钱包地址不能为空");
        }
        wallet.setUserId(userId);
        wallet.setStatus(1);
        if (wallet.getIsDefault() == null) wallet.setIsDefault(0);

        if (wallet.getIsDefault() == 1) {
            List<UserWallet> defaults = walletMapper.selectList(
                    new LambdaQueryWrapper<UserWallet>().eq(UserWallet::getUserId, userId).eq(UserWallet::getIsDefault, 1)
            );
            for (UserWallet w : defaults) {
                w.setIsDefault(0);
                walletMapper.updateById(w);
            }
        }
        walletMapper.insert(wallet);
        return Result.success(wallet);
    }

    /**
     * 删除钱包地址
     * POST /api/wap/user-wallet/delete
     */
    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody Map<String, Long> params) {
        Long userId = UserContext.getUserId();
        Long id = params.get("id");
        UserWallet wallet = walletMapper.selectById(id);
        if (wallet == null || !wallet.getUserId().equals(userId)) throw new BusinessException("钱包地址不存在");
        walletMapper.deleteById(id);
        return Result.success();
    }
}
