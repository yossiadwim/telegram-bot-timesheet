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

    List<String> validCommands = List.of("/start", "/help", "/timesheet", "/stop", "document");


    public Message(String messageTextFromClient, Long chatId) {
        this.messageTextFromClient = messageTextFromClient;
        this.chatId = chatId;
    }

    public Message(String messageTextToClient, Long chatId, String customMessage){
        this.messageTextToClient = messageTextToClient;
        this.chatId = chatId;
        this.customMessage = customMessage;
    }

    public String getCommand() {
        if (messageTextFromClient == null) {
            return "document";
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
                case "/start" -> messageTextToClient = "Hello, I am Timesheet bot, what do you want to do?";
                case "/help" -> messageTextToClient = "I am Timesheet bot, what do you want to do?";
                case "/timesheet" -> messageTextToClient = "Enter your timesheet";
                case "/stop" -> messageTextToClient = "Goodbye";
                case "document" -> messageTextToClient = "File received \n \nSend me your email and password\nEmail: example@mail.com\nPassword: password123";
                default -> messageTextToClient = "I don't understand you, please try again";
            }
        }

        return messageTextToClient;
    }

}
