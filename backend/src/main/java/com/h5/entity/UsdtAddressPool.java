package com.h5.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * USDT充值地址池
 */
@Data
@TableName("usdt_address_pool")
public class UsdtAddressPool {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String address;

    /** TRC20 / ERC20 */
    private String chain;

    private String label;

    private Integer status;

    private LocalDateTime lastUsedTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
