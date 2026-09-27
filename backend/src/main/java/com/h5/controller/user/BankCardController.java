package com.h5.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h5.common.BusinessException;
import com.h5.common.Result;
import com.h5.common.UserContext;
import com.h5.entity.UserBankCard;
import com.h5.mapper.UserBankCardMapper;
import com.h5.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 银行卡 Controller（委托 UserService，setDefault 保留 Controller 层）
 */
@RestController
@RequestMapping("/wap/user-bank-card")
public class BankCardController {

    @Autowired private UserService userService;
    @Autowired private UserBankCardMapper bankCardMapper;

    /** 银行卡列表 */
    @GetMapping("/list")
    public Result<List<UserBankCard>> list() {
        return Result.success(userService.getBankCards(UserContext.getUserId()));
    }

    /** 添加银行卡 */
    @PostMapping("/add")
    @Transactional
    public Result<UserBankCard> add(@RequestBody UserBankCard card) {
        return Result.success(userService.addBankCard(
                UserContext.getUserId(),
                card.getBankName(), card.getBranchName(),
                card.getCardNumber(), card.getCardHolder(), card.getPhone()
        ));
    }

    /** 设为默认 */
    @PostMapping("/set-default")
    @Transactional
    public Result<Void> setDefault(@RequestBody Map<String, Long> params) {
        Long userId = UserContext.getUserId();
        Long id = params.get("id");
        UserBankCard card = bankCardMapper.selectById(id);
        if (card == null || !card.getUserId().equals(userId)) throw new BusinessException("银行卡不存在");

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

    /** 删除银行卡 */
    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody Map<String, Long> params) {
        userService.deleteBankCard(UserContext.getUserId(), params.get("id"));
        return Result.success();
    }
}
