package com.h5.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("draw_results")
public class DrawResult {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String lotteryCode;
    private String period;
    private String numbers;
    private LocalDateTime drawTime;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
