package com.h5.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 管理员操作日志
 */
@Data
@TableName("admin_operation_log")
public class AdminOperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long adminId;

    private String adminName;

    /** 操作类型 */
    private String action;

    /** 操作对象类型 user/order/bet/... */
    private String targetType;

    private Long targetId;

    /** 变更前JSON */
    private String beforeData;

    /** 变更后JSON */
    private String afterData;

    private String ip;

    private String userAgent;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
