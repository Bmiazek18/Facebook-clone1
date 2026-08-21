package com.facebook.FeedService.scheduler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Component
@Slf4j
public class UploadCleanupScheduler {

    @Value("${app.upload.dir}")
    private String uploadDir;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Runs every 5 minutes
    @Scheduled(fixedDelay = 300000)
    public void cleanupUnfinishedUploads() {
        log.info("Starting cleanup task for unfinished uploads in: {}", uploadDir);

        File dir = new File(uploadDir);
        if (!dir.exists() || !dir.isDirectory()) {
            log.warn("Upload directory does not exist: {}", uploadDir);
            return;
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".info"));
        if (files == null) {
            return;
        }

        long oneHourAgo = System.currentTimeMillis() - (3600 * 1000); // 1 hour in milliseconds
        int deletedCount = 0;

        for (File infoFile : files) {
            // Check if metadata file was last modified more than 1 hour ago
            if (infoFile.lastModified() < oneHourAgo) {
                try {
                    // Read and parse metadata JSON
                    Map<String, Object> info = objectMapper.readValue(infoFile, new TypeReference<Map<String, Object>>() {});
                    Number offset = (Number) info.get("Offset");
                    Number size = (Number) info.get("Size");

                    if (offset != null && size != null && offset.longValue() < size.longValue()) {
                        // Upload is unfinished! Clean it up.
                        String fileId = infoFile.getName().substring(0, infoFile.getName().length() - ".info".length());
                        File dataFile = new File(dir, fileId);

                        log.info("Deleting unfinished upload: ID={}, Offset={}, Size={}, LastModified={}", 
                                fileId, offset, size, infoFile.lastModified());

                        boolean infoDeleted = infoFile.delete();
                        boolean dataDeleted = false;
                        if (dataFile.exists()) {
                            dataDeleted = dataFile.delete();
                        }

                        if (infoDeleted || dataDeleted) {
                            deletedCount++;
                        }
                    }
                } catch (IOException e) {
                    log.error("Failed to parse .info file: " + infoFile.getAbsolutePath(), e);
                }
            }
        }

        if (deletedCount > 0) {
            log.info("Cleanup completed. Deleted {} unfinished upload(s) older than 1 hour.", deletedCount);
        } else {
            log.info("Cleanup completed. No unfinished uploads older than 1 hour found.");
        }
    }
}
