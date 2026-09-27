package com.h5.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("promotions")
public class Promotion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String category;
    private String categoryName;
    private String image;
    private String description;
    private String content;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer sort;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
