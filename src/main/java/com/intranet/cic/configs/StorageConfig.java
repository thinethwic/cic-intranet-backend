package com.intranet.cic.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;

@Configuration
public class StorageConfig {

    @Value("${file.upload-dir:/uploads}")
    private String uploadDir;

    @PostConstruct
    public void init() throws IOException {
        // Create upload directories on startup
        Files.createDirectories(Paths.get(uploadDir, "images"));
        Files.createDirectories(Paths.get(uploadDir, "documents"));
    }
}