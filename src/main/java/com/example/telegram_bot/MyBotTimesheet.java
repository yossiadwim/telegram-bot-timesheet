package com.example.telegram_bot;

import com.example.telegram_bot.service.DocumentHandlerService;
import com.example.telegram_bot.service.MessageHandlerService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;


@Slf4j
@Component
public class MyBotTimesheet implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private static final String DOC_ACCEPTED = "document accepted";
    private static final String NOT_VALID_FILE = "not valid file";
    private static final String NOT_VALID_CAPTION = "not valid caption";
    private static final String DOC_PROCESS = "document process";
    private static final String DOC_FINISHED = "document finished";
    private static final String ERROR_PROCESS = "Terjadi kesalahan saat memproses file.";


    @Value("${spring.telegram.bot.token}")
    private String botToken;

    @Autowired
    private MessageHandlerService messageHandlerService;

    @Autowired
    private DocumentHandlerService documentHandlerService;

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

        if (update.hasMessage() && (update.getMessage().hasText() || update.getMessage().hasDocument())) {
            Long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();
            String username = update.getMessage().getFrom().getUserName();

            if (update.getMessage().hasText()) {
                try {
                    messageHandlerService.handleMessage(messageText,chatId);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else if(update.getMessage().hasDocument()){
                String fileName = update.getMessage().getDocument().getFileName();
                if (fileName == null || !fileName.contains(".")) {
                    sendMessageToClientByKeyword(NOT_VALID_FILE, chatId);
                    return;
                }
                String extension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.') + 1) : "";
                sendMessageToClientByKeyword(DOC_ACCEPTED, chatId);

                if (!extension.contentEquals("xlsx")){
                    sendMessageToClientByKeyword(NOT_VALID_FILE, chatId);
                } else{
                    if (!documentHandlerService.validationCaption(update.getMessage().getCaption())){
                        sendMessageToClientByKeyword(NOT_VALID_CAPTION, chatId);
                    }else{
                        try {
                            sendMessageToClientByKeyword(DOC_PROCESS,chatId);
                            documentHandlerService.handleDocument(update, username, chatId);
                            sendMessageToClientByKeyword(DOC_FINISHED, chatId);
                        } catch (IOException e) {
                            sendMessageToClientByKeyword(ERROR_PROCESS, chatId);
                        }
                    }
                }
            }
        }
    }

    private void sendMessageToClientByKeyword(String message, Long chatId) {
        try{
            messageHandlerService.sendMessageByKeyword(message, chatId);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

}


