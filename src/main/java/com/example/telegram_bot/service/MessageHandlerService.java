package com.example.telegram_bot.service;

import com.example.telegram_bot.dto.MessageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;


@Service
@RequiredArgsConstructor
public class MessageHandlerService {

    private final TelegramClientService telegramClientService;
    public String messageTextToClient;

    public MessageDTO message(String messageTextFromClient, Long chatId) throws TelegramApiException {
        String messageTextToClient = extractCommand(messageTextFromClient, chatId);
        return MessageDTO.builder().messageTextToClient(messageTextToClient).chatId(chatId).build();
    }

    private String extractCommand(String messageTextFromClient, Long chatId) throws TelegramApiException {
        return switch (messageTextFromClient) {
            case "/start" -> messageTextToClient = """
        👋 Selamat datang di *Timesheet Bot*!\s

        Silakan upload file Excel timesheet Anda dengan format yang sesuai.\s
        Jangan lupa tambahkan *caption* dalam format:
       \s
       \s
        Email: your@email.com
        Password: yourpassword
       \s

       Silahkan ketik /template untuk melihat template timesheet.
       \s""";
            case "/template" -> {
                sendTemplate(chatId);
                yield null;
            }
            case "/stop" -> messageTextToClient = "Terima kasih sudah menggunakan bot ini";
            case "document accepted" -> messageTextToClient = "File diterima";
            case "document process" -> messageTextToClient = "Timesheet sedang diproses... mohon tunggu ya";
            case "document finished" -> messageTextToClient = "Timesheet berhasil diisi";
            case "not valid caption" -> messageTextToClient = "Caption tidak valid";
            case "not valid file" -> messageTextToClient = "File tidak valid atau tidak berformat .xlsx";
            default -> messageTextToClient = "Perintah tidak dikenali";
        };
    }

    public void handleMessage(String messageTextFromClient, Long chatId) throws TelegramApiException {

        MessageDTO messageDTO = message(messageTextFromClient, chatId);
        if (messageTextFromClient != null && !messageTextFromClient.isBlank()) {
            sendMessage(messageDTO.getMessageTextToClient(), messageDTO.getChatId());
        }

    }

    public void sendMessage(String messageTextFromClient, Long chatId) throws TelegramApiException {
        if (messageTextFromClient == null || messageTextFromClient.isBlank()) return;
        SendMessage sendMessageBuilder = SendMessage.builder()
                .chatId(chatId)
                .text(messageTextFromClient)
                .parseMode("Markdown")
                .build();
        telegramClientService.sendMessage(sendMessageBuilder);
    }

    public void sendMessageByKeyword(String keyword, Long chatId) throws TelegramApiException {
        String text = extractCommand(keyword, chatId);
        if (text == null || text.isBlank()) return;
        SendMessage sendMessageBuilder = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("Markdown")
                .build();
        telegramClientService.sendMessage(sendMessageBuilder);
    }



    public void sendTemplate(Long chatId) throws TelegramApiException {
        try {
            File file = new ClassPathResource("templates/timesheet.xlsx").getFile();
            SendDocument sendDocumentBuilder = SendDocument.builder()
                    .chatId(chatId)
                    .document(new InputFile(file, "timesheet.xlsx"))
                    .caption("Email: your@email.com\n" +
                            "Password: yourpassword")
                    .build();
            telegramClientService.sendDocument(sendDocumentBuilder);
        } catch (Exception e) {
            throw new RuntimeException("Gagal membaca file dari resources", e);
        }
    }
}
