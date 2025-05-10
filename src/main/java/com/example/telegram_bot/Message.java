package com.example.telegram_bot;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Getter
@Setter
@Slf4j
public class Message {

    private String messageTextFromClient;
    private String messageTextToClient;
    private Long chatId;
    private String customMessage;

    List<String> validCommands = List.of("/start", "/stop", "document", "not valid file", "not valid caption", "document accepted", "document process", "document finished");

    public Message(String messageTextFromClient, Long chatId) {
        this.messageTextFromClient = messageTextFromClient;
        this.chatId = chatId;
    }

    public String getCommand() {
        if (messageTextFromClient.contentEquals("not valid file")){
            return "not valid file";
        }else if(messageTextFromClient.contentEquals("not valid caption")){
            return "not valid caption";
        }else if(messageTextFromClient.contentEquals("accepted")){
            return "document accepted";
        }else if(messageTextFromClient.contentEquals("File diproses, mohon tunggu ya")){
            return "document process";
        }else if(messageTextFromClient.contentEquals("Timesheet berhasil diisi")){
            return "document finished";
        }
        return messageTextFromClient.split(" ")[0];
    }

    public Boolean validateCommand(String command) {
        return validCommands.contains(command);
    }

    public String getMessage() {
        String command = getCommand();
        Boolean isValidCommand = validateCommand(command);
        if(Boolean.TRUE.equals(isValidCommand)){
            switch (command) {
                case "/start" -> messageTextToClient = """
        👋 Selamat datang di *Timesheet Bot*!\s

        Silakan upload file Excel timesheet Anda dengan format yang sesuai.\s
        Jangan lupa tambahkan *caption* dalam format:
       \s
       \s
        Email: your@email.com
        Password: yourpassword
       \s

        Bot ini akan otomatis mengisi form Timesheet berdasarkan data tersebut.
       \s""";
                case "/stop" -> messageTextToClient = "Terima kasih sudah menggunakan bot ini";
                case "document accepted" -> messageTextToClient = "File diterima";
                case "document process" -> messageTextToClient = "Timesheet sedang diproses... mohon tunggu ya";
                case "document finished" -> messageTextToClient = "Timesheet berhasil diisi";
                case "not valid caption" -> messageTextToClient = "Caption tidak valid";
                case "not valid file" -> messageTextToClient = "File tidak valid atau tidak berformat .xlsx";
                default -> messageTextToClient = "Perintah tidak dikenali";
            }
        }else{
            messageTextToClient = "Perintah tidak dikenali";
        }

        return messageTextToClient;
    }

}
