package com.example.telegram_bot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Service
public class TelegramClientService  {

    private final TelegramClient telegramClient;

    public TelegramClientService(@Value("${spring.telegram.bot.token}") String botToken) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
    }

    public void send(SendMessage sendMessage) throws TelegramApiException {
        telegramClient.execute(sendMessage);
    }

    public TelegramClient getClient() {
        return telegramClient;
    }

}
