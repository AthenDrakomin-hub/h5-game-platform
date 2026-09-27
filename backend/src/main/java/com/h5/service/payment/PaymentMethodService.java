package com.h5.service.payment;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h5.entity.PaymentMethod;
import com.h5.mapper.PaymentMethodMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 支付方式服务
 * 从数据库 payment_methods 表读取支付方式配置，替代硬编码
 */
@Service
public class PaymentMethodService {

    @Autowired
    private PaymentMethodMapper paymentMethodMapper;

    /**
     * 获取充值方式列表
     */
    public Map<String, Object> getRechargeMethods() {
        List<PaymentMethod> all = paymentMethodMapper.selectList(
                new LambdaQueryWrapper<PaymentMethod>()
                        .eq(PaymentMethod::getStatus, 1)
                        .ne(PaymentMethod::getCategory, "withdraw") // 排除仅提现的
                        .orderByAsc(PaymentMethod::getSort)
        );

        Map<String, Object> data = new HashMap<>();
        data.put("cnyMethods", filterByType(all, "cny"));
        data.put("cryptoMethods", filterByType(all, "crypto"));
        return data;
    }

    /**
     * 获取提现方式列表
     */
    public Map<String, Object> getWithdrawMethods() {
        List<PaymentMethod> all = paymentMethodMapper.selectList(
                new LambdaQueryWrapper<PaymentMethod>()
                        .eq(PaymentMethod::getStatus, 1)
                        .ne(PaymentMethod::getCategory, "recharge") // 排除仅充值的
                        .orderByAsc(PaymentMethod::getSort)
        );

        Map<String, Object> data = new HashMap<>();
        data.put("cnyMethods", filterByType(all, "cny"));
        data.put("cryptoMethods", filterByType(all, "crypto"));
        data.put("withdrawCnyEnabled", hasType(all, "cny") ? 1 : 0);
        data.put("withdrawCryptoEnabled", hasType(all, "crypto") ? 1 : 0);
        return data;
    }

    /**
     * 根据编码获取支付方式
     */
    public PaymentMethod getByCode(String code) {
        return paymentMethodMapper.selectOne(
                new LambdaQueryWrapper<PaymentMethod>().eq(PaymentMethod::getCode, code)
        );
    }

    /**
     * 获取所有启用的支付方式（管理端用）
     */
    public List<PaymentMethod> getAllEnabled() {
        return paymentMethodMapper.selectList(
                new LambdaQueryWrapper<PaymentMethod>()
                        .eq(PaymentMethod::getStatus, 1)
                        .orderByAsc(PaymentMethod::getSort)
        );
    }

    private List<Map<String, Object>> filterByType(List<PaymentMethod> list, String type) {
        return list.stream()
                .filter(m -> type.equals(m.getType()))
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    private boolean hasType(List<PaymentMethod> list, String type) {
        return list.stream().anyMatch(m -> type.equals(m.getType()));
    }

    private Map<String, Object> toMap(PaymentMethod m) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", m.getId());
        map.put("name", m.getName());
        map.put("code", m.getCode());
        map.put("icon", m.getIcon());
        map.put("minAmount", m.getMinAmount());
        map.put("maxAmount", m.getMaxAmount());
        map.put("feeRate", m.getFeeRate());
        map.put("status", m.getStatus());
        if (m.getAddress() != null && !m.getAddress().isEmpty()) {
            map.put("address", m.getAddress());
        }
        if (m.getAddressName() != null && !m.getAddressName().isEmpty()) {
            map.put("addressName", m.getAddressName());
        }
        if (m.getQrcode() != null && !m.getQrcode().isEmpty()) {
            map.put("qrcode", m.getQrcode());
        }
        if (m.getChain() != null && !m.getChain().isEmpty()) {
            map.put("chain", m.getChain());
        }
        map.put("autoConfirm", m.getAutoConfirm() != null && m.getAutoConfirm() == 1);
        return map;
    }
}
