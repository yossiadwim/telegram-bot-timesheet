package com.example.telegram_bot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@Slf4j
public class DeleteSchedulerService {

    private static final String DIRECTORY_PATH = "downloads";

    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Jakarta")
    public void cleanUpFiles(){
        File directory = new File(DIRECTORY_PATH);
        if (!directory.exists() && !directory.isDirectory()) {
            log.warn("Directory does not exist: {}", directory);
            return;
        }

        File[] files = directory.listFiles();
        if (files == null) return;
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        for (File file : files) {
            if (file.isFile()){
                LocalDateTime lastModifiedTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(file.lastModified()),
                        ZoneId.systemDefault()
                );
                if (lastModifiedTime.isBefore(oneHourAgo)){
                    boolean deleted = file.delete();
                    if (deleted){
                        log.info("File deleted: {}", file.getName());
                    }else {
                        log.error("Failed to delete file: {}", file.getName());
                    }
                }
            }
        }
    }


}
