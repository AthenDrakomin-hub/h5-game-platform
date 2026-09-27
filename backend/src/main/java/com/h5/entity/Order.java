package com.h5.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private String type;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal actualAmount;
    private String method;
    private String methodName;
    private String payAccount;
    private String payQrcode;
    private String userAccount;
    private String status;
    private String remark;
    private String adminRemark;
    private LocalDateTime auditTime;
    private Long auditBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
