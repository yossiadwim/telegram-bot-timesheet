package com.example.telegram_bot.service;

import com.example.telegram_bot.BotMessageKeyword;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

@Component
public class UpdateHandlerService {

    @Autowired
    private MessageHandlerService messageHandlerService;

    @Autowired
    private DocumentHandlerService documentHandlerService;

    public void handleUpdate(Update update) throws TelegramApiException {
        if (update.hasMessage() && (update.getMessage().hasText() || update.getMessage().hasDocument())) {
            Long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();
            String username = update.getMessage().getFrom().getUserName();

            if (update.getMessage().hasText()) {
                messageHandlerService.handleMessage(messageText, chatId);
            } else if (update.getMessage().hasDocument()) {
                String fileName = update.getMessage().getDocument().getFileName();
                if (fileName == null || !fileName.contains(".")) {
                    sendMessageToClientByKeyword(BotMessageKeyword.NOT_VALID_FILE.getMessage(), chatId);
                    return;
                }
                String extension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.') + 1) : "";
                sendMessageToClientByKeyword(BotMessageKeyword.DOC_ACCEPTED.getMessage(), chatId);

                if (!extension.contentEquals("xlsx")) {
                    sendMessageToClientByKeyword(BotMessageKeyword.NOT_VALID_FILE.getMessage(), chatId);
                } else {
                    if (!documentHandlerService.validationCaption(update.getMessage().getCaption())) {
                        sendMessageToClientByKeyword(BotMessageKeyword.NOT_VALID_CAPTION.getMessage(), chatId);
                    } else {
                        sendMessageToClientByKeyword(BotMessageKeyword.DOC_PROCESS.getMessage(), chatId);
                        docProcess(update, username, chatId);
                        sendMessageToClientByKeyword(BotMessageKeyword.DOC_FINISHED.getMessage(), chatId);
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

    private void docProcess (Update update, String username, Long chatId) {
        try{
            documentHandlerService.handleDocument(update, username, chatId);
        } catch (IOException e) {
            sendMessageToClientByKeyword(BotMessageKeyword.ERROR_PROCESS.getMessage(), chatId);
            throw new RuntimeException(e);
        }
    }
}
