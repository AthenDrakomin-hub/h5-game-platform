package com.h5.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String avatar;

    private String phone;

    private String email;

    private BigDecimal balance;

    private BigDecimal frozenBalance;

    private Integer vipLevel;

    private Integer isTrial;

    private Integer status;

    /** 角色：user普通用户 / admin管理员 / superadmin超级管理员 */
    private String role;

    private String fundPassword;

    private String inviteCode;

    private Long invitedBy;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 乐观锁版本号 */
    @Version
    private Integer version;

    @TableLogic
    private Integer deleted;
}
