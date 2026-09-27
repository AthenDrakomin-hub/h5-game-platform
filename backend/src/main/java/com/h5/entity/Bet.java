package com.h5.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@TableName("bets")
public class Bet {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String betNo;
    private Long userId;
    private String lotteryCode;
    private String period;
    private String playType;
    private String numbers;
    private BigDecimal amount;
    private BigDecimal odds;
    private BigDecimal winAmount;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
