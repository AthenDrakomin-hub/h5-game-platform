package com.h5.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("lotteries")
public class Lottery {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String code;
    private String categoryCode;
    private String categoryName;
    private String icon;
    private Integer drawInterval;
    private Integer closeTime;
    private Integer status;
    private Integer sort;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
