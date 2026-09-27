package com.h5.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付方式
 */
@Data
@TableName("payment_methods")
public class PaymentMethod {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String code;

    /** cny法币 / crypto数字货币 */
    private String type;

    /** recharge充值 / withdraw提现 / both两者 */
    private String category;

    private String icon;

    private BigDecimal minAmount;

    private BigDecimal maxAmount;

    private BigDecimal feeRate;

    private BigDecimal fixedFee;

    /** 收款地址 */
    private String address;

    /** 收款人姓名/开户行 */
    private String addressName;

    private String qrcode;

    /** 0人工审核 1自动确认(USDT链上监听) */
    private Integer autoConfirm;

    /** TRC20 / ERC20 */
    private String chain;

    /** 三方支付API配置JSON */
    private String apiConfig;

    private Integer status;

    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
