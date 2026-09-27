package com.h5.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("bot_telegram_user")
public class BotTelegramUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long telegramId;

    private Long userId;

    private String username;

    private String firstName;

    private String lastName;

    private String photoUrl;

    private String languageCode;

    private Integer isPremium;

    private String inviteCode;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
