package com.example.telegram_bot.service;

import com.example.telegram_bot.dto.DocumentDTO;
import com.example.telegram_bot.dto.UserDTO;
import com.example.timesheet.Timesheet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
public class DocumentHandlerService {

    private final TelegramClientService telegramClientService;
    private final Timesheet timesheet;

    public Boolean validationCaption(String caption){
        if (caption == null || !caption.contains("\n")) {
            return false;
        }

        String[] lines = caption.split("\n");
        return lines.length == 2 && lines[0].startsWith("Email: ") && lines[1].startsWith("Password: ");
    }


    public UserDTO extractCaption(String caption) {
        String[] lines = caption.split("\n");

        String email = lines[0].replaceFirst("Email: ", "").trim();
        String password = lines[1].replaceFirst("Password: ", "").trim();

        return UserDTO.builder()
                .email(email)
                .password(password)
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

        DocumentDTO documentDTO = saveDocument(fileName,username,fileId,chatId);

        if ((!email.isEmpty() && !password.isEmpty()) && Boolean.TRUE.equals(documentDTO.getIsSaved())) {
            processTimesheet(documentDTO, email, password);
        }

    }

    public DocumentDTO saveDocument(String fileName, String username, String fileId, Long chatId){

        try {
            String extension = fileName.split("\\.")[1];
            String savedFileName = String.format("%s_%s_%s.%s",
                    fileName.split("\\.")[0], username, chatId, extension);

            File file = telegramClientService.getClient().execute(new GetFile(fileId));
            InputStream inputStream = telegramClientService.getClient().downloadFileAsStream(file);

            Path outputPath = Paths.get("downloads", savedFileName);
            Files.createDirectories(outputPath.getParent());
            Files.copy(inputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);

            return DocumentDTO.builder().filePath(outputPath.toString()).isSaved(true).build();

        } catch (IOException | TelegramApiException e) {
            return DocumentDTO.builder().filePath(null).isSaved(false).build();
        }
    }

    private void processTimesheet(DocumentDTO document, String email, String password) throws IOException {
        timesheet.login(email, password);
        timesheet.fillTimesheet(document.getFilePath());
    }


}
