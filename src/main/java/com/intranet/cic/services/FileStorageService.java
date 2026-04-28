package com.intranet.cic.services;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileStorageService {
    String storeDocument(MultipartFile file);
    String storeImage(MultipartFile file);
    void deleteFile(String fileUrl);
    Path resolveFilePath(String fileUrl);
}
