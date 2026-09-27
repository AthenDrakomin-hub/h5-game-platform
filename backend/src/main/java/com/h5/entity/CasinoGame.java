package com.h5.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("casino_games")
public class CasinoGame {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String gameCode;
    private String providerCode;
    private String providerName;
    private String category;
    private String icon;
    private String enterUrl;
    private Integer isHot;
    private Integer isNew;
    private Integer status;
    private Integer sort;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
