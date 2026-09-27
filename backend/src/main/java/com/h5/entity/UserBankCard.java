package com.h5.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data
@TableName("user_bank_cards")
public class UserBankCard {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String bankName;
    private String branchName;
    private String cardNumber;
    private String cardHolder;
    private String phone;
    private Integer isDefault;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
