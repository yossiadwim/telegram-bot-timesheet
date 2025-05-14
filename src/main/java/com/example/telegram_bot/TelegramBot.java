package com.example.telegram_bot;

import com.example.telegram_bot.service.DeleteSchedulerService;
import com.example.telegram_bot.service.UpdateHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@Slf4j
@Component
public class TelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    @Value("${spring.telegram.bot.token}")
    private String botToken;

    @Autowired
    private UpdateHandlerService updateHandlerService;

    @Autowired
    private DeleteSchedulerService deleteSchedulerService;

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        try {
            updateHandlerService.handleUpdate(update);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


}


