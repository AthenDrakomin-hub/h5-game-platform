package com.h5.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h5.common.BusinessException;
import com.h5.common.Result;
import com.h5.common.UserContext;
import com.h5.entity.UserBankCard;
import com.h5.entity.UserWallet;
import com.h5.mapper.UserBankCardMapper;
import com.h5.mapper.UserWalletMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wap/user-bank-card")
public class BankCardController {

    @Autowired
    private UserBankCardMapper bankCardMapper;

    /**
     * 银行卡列表
     * GET /api/wap/user-bank-card/list
     */
    @GetMapping("/list")
    public Result<List<UserBankCard>> list() {
        Long userId = UserContext.getUserId();
        List<UserBankCard> list = bankCardMapper.selectList(
                new LambdaQueryWrapper<UserBankCard>()
                        .eq(UserBankCard::getUserId, userId)
                        .eq(UserBankCard::getStatus, 1)
                        .orderByDesc(UserBankCard::getIsDefault)
                        .orderByDesc(UserBankCard::getCreateTime)
        );
        return Result.success(list);
    }

    /**
     * 添加银行卡
     * POST /api/wap/user-bank-card/add
     */
    @PostMapping("/add")
    @Transactional
    public Result<UserBankCard> add(@RequestBody UserBankCard card) {
        Long userId = UserContext.getUserId();
        if (card.getBankName() == null || card.getCardNumber() == null || card.getCardHolder() == null) {
            throw new BusinessException("银行名称、卡号、持卡人不能为空");
        }
        card.setUserId(userId);
        card.setStatus(1);
        if (card.getIsDefault() == null) card.setIsDefault(0);

        // 如果设为默认，取消其他默认
        if (card.getIsDefault() == 1) {
            List<UserBankCard> defaults = bankCardMapper.selectList(
                    new LambdaQueryWrapper<UserBankCard>().eq(UserBankCard::getUserId, userId).eq(UserBankCard::getIsDefault, 1)
            );
            for (UserBankCard c : defaults) {
                c.setIsDefault(0);
                bankCardMapper.updateById(c);
            }
        }
        bankCardMapper.insert(card);
        return Result.success(card);
    }

    /**
     * 设为默认
     * POST /api/wap/user-bank-card/set-default
     */
    @PostMapping("/set-default")
    @Transactional
    public Result<Void> setDefault(@RequestBody Map<String, Long> params) {
        Long userId = UserContext.getUserId();
        Long id = params.get("id");
        UserBankCard card = bankCardMapper.selectById(id);
        if (card == null || !card.getUserId().equals(userId)) throw new BusinessException("银行卡不存在");

        // 取消其他默认
        List<UserBankCard> defaults = bankCardMapper.selectList(
                new LambdaQueryWrapper<UserBankCard>().eq(UserBankCard::getUserId, userId).eq(UserBankCard::getIsDefault, 1)
        );
        for (UserBankCard c : defaults) {
            c.setIsDefault(0);
            bankCardMapper.updateById(c);
        }
        card.setIsDefault(1);
        bankCardMapper.updateById(card);
        return Result.success();
    }

    /**
     * 删除银行卡
     * POST /api/wap/user-bank-card/delete
     */
    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody Map<String, Long> params) {
        Long userId = UserContext.getUserId();
        Long id = params.get("id");
        UserBankCard card = bankCardMapper.selectById(id);
        if (card == null || !card.getUserId().equals(userId)) throw new BusinessException("银行卡不存在");
        bankCardMapper.deleteById(id);
        return Result.success();
    }
}
