package com.example.telegram_bot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nonnull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveDocumentDTO {

    private String filePath;
    private Boolean isSaved;
}
