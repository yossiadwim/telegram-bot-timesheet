package com.example.telegram_bot;

import com.example.telegram_bot.dto.SaveDocumentDTO;
import com.example.telegram_bot.dto.UserDTO;
import com.example.telegram_bot.service.TelegramClientService;
import com.example.timesheet.Timesheet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;


@Slf4j
@Component
public class MyBotTimesheet implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    @Value("${spring.telegram.bot.token}")
    private String botToken;

    private final TelegramClientService telegramClientService;
    private final Timesheet timesheet;

    @Autowired
    public MyBotTimesheet(TelegramClientService telegramClientService, Timesheet timesheet) {
        this.telegramClientService = telegramClientService;
        this.timesheet = timesheet;
    }

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

            if (update.getMessage().hasDocument()) {
                try {
                    handleDocument(update, chatId);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            Message message= new Message(messageText,chatId);
            sendMessage(message);

            try {
                telegramClientService.send(sendMessage(message));
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }

        }
    }

    public SendMessage sendMessage(Message message){
        return SendMessage
                .builder()
                .chatId(String.valueOf(message.getChatId()))
                .text(message.getMessage())
                .build();
    }

    public void handleDocument(Update update, Long chatId) throws IOException {
        String fileName = update.getMessage().getDocument().getFileName();
        String fileId = update.getMessage().getDocument().getFileId();
        String caption = update.getMessage().getCaption();
        String email = "";
        String password = "";

        if (caption.contains("\n")){
            UserDTO userDTO = extractUser(caption);
            email = userDTO.getEmail();
            password = userDTO.getPassword();
        }

        SaveDocumentDTO save = saveDocument(fileName,fileId, chatId);

        if ((!email.isEmpty() && !password.isEmpty()) && Boolean.TRUE.equals(save.getIsSaved())) {
            timesheet.login(email, password);
            timesheet.fillTimesheet(save.getFilePath());
        }

    }

    public SaveDocumentDTO saveDocument(String fileName, String fileId, Long chatId){

        try {
            String fileNameSave = fileName.split("\\.")[0];
            String extension = fileName.split("\\.")[1];

            File file = telegramClientService.getClient().execute(new GetFile(fileId));
            InputStream inputStream = telegramClientService.getClient().downloadFileAsStream(file);

            Path outputPath = Paths.get("downloads", fileNameSave + "_" + chatId+"."+extension);
            Files.createDirectories(outputPath.getParent());
            Files.copy(inputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);

            return SaveDocumentDTO.builder().filePath(outputPath.toString()).isSaved(true).build();

        } catch (TelegramApiException | IOException e) {
            return SaveDocumentDTO.builder().filePath(null).isSaved(false).build();
        }
    }

    public UserDTO extractUser (String caption){
        String email = caption.split("\n")[0];
        String password = caption.split("\n")[1];

        String validEmail = email.split(": ")[1];
        String validPassword = password.split(": ")[1];

        return UserDTO.builder().email(validEmail).password(validPassword).build();

    }
}


