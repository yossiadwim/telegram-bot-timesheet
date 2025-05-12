package com.example.telegram_bot.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageDTO {
    private String messageTextToClient;
    private String messageTextFromClient;
    private Long chatId;

}
