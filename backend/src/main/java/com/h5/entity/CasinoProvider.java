package com.h5.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("casino_providers")
public class CasinoProvider {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String code;
    private String icon;
    private Integer status;
    private Integer sort;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableLogic
    private Integer deleted;
}
