package com.example.telegram_bot;

import lombok.Getter;

@Getter
public enum BotMessageKeyword {

    DOC_ACCEPTED("document accepted"),
    NOT_VALID_FILE("not valid file"),
    NOT_VALID_CAPTION("not valid caption"),
    DOC_PROCESS("document process"),
    DOC_FINISHED("document finished"),
    ERROR_PROCESS("Terjadi kesalahan saat memproses file.");

    private final String message;

    BotMessageKeyword(String message) {
        this.message = message;
    }

}
