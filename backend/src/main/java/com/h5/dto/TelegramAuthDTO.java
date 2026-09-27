package com.h5.dto;

import lombok.Data;

@Data
public class TelegramAuthDTO {

    private Long telegramId;

    private String username;

    private String firstName;

    private String lastName;

    private String photoUrl;

    private String languageCode;

    private String inviteCode;
}
