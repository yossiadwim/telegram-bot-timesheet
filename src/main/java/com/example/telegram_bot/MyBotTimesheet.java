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
            String username = update.getMessage().getFrom().getUserName();
            if (update.getMessage().hasDocument()) {
                String fileName = update.getMessage().getDocument().getFileName();
                String extension = fileName.split("\\.")[1];
                String messageDocumentAccepted = "accepted";
                sendTelegramMessage(messageDocumentAccepted, chatId);
                if (!extension.contentEquals("xlsx")){
                    String messageTextNotValid = "not valid file";
                    sendTelegramMessage(messageTextNotValid, chatId);
                } else{
                    if (!validationCaption(update.getMessage().getCaption())){
                        String messageTextNotValid = "not valid caption";
                        sendTelegramMessage(messageTextNotValid, chatId);
                    }else{
                        try {
                            String messageDocumentProcess = "File diproses, mohon tunggu ya";
                            sendTelegramMessage(messageDocumentProcess,chatId);
                            handleDocument(update, username, chatId);
                            sendTelegramMessage("Timesheet berhasil diisi", chatId);
                        } catch (IOException e) {
                            sendTelegramMessage("Terjadi kesalahan saat memproses file.", chatId);
                        }
                    }
                }
            } else if(update.getMessage().hasText()){
                Message message = new Message(messageText, chatId);
                try {
                    telegramClientService.send(sendMessageToUser(message));
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public SendMessage sendMessageToUser(Message message){
        return SendMessage
                .builder()
                .chatId(String.valueOf(message.getChatId()))
                .text(message.getMessage())
                .parseMode("Markdown")
                .build();
    }

    public void handleDocument(Update update, String username, Long chatId) throws IOException {
        String fileName = update.getMessage().getDocument().getFileName();
        String fileId = update.getMessage().getDocument().getFileId();
        String caption = update.getMessage().getCaption();
        String email = "";
        String password = "";

        if (Boolean.TRUE.equals(validationCaption(caption))){
            UserDTO user = extractCaption(caption);
            email = user.getEmail();
            password = user.getPassword();
        }

        SaveDocumentDTO save = saveDocument(fileName,username,fileId,chatId);

        if ((!email.isEmpty() && !password.isEmpty()) && Boolean.TRUE.equals(save.getIsSaved())) {
            timesheet.login(email, password);
            timesheet.fillTimesheet(save.getFilePath());
        }

    }

    public SaveDocumentDTO saveDocument(String fileName, String username, String fileId, Long chatId){

        try {
            String fileNameSave = fileName.split("\\.")[0];
            String extension = fileName.split("\\.")[1];

            File file = telegramClientService.getClient().execute(new GetFile(fileId));
            InputStream inputStream = telegramClientService.getClient().downloadFileAsStream(file);

            Path outputPath = Paths.get("downloads", fileNameSave + "_" + username + "_"+ chatId+"."+extension);
            Files.createDirectories(outputPath.getParent());
            Files.copy(inputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);

            return SaveDocumentDTO.builder().filePath(outputPath.toString()).isSaved(true).build();

        } catch (TelegramApiException | IOException e) {
            return SaveDocumentDTO.builder().filePath(null).isSaved(false).build();
        }
    }

    public UserDTO extractCaption (String caption){
        String email = caption.split("\n")[0];
        String password = caption.split("\n")[1];

        String validEmail = email.split(": ")[1];
        String validPassword = password.split(": ")[1];

        return UserDTO.builder().email(validEmail).password(validPassword).build();

    }

    public Boolean validationCaption(String caption){
        if (caption == null){
            return false;
        }
        else if (caption.contains("\n")){
            return caption.contains("Email: ") && caption.contains("Password: ");
        }
        return false;
    }


    public void sendTelegramMessage(String message, Long chatId){
        try {
            telegramClientService.send(sendMessageToUser(new Message(message, chatId)));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


}


