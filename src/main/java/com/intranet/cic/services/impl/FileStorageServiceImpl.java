package com.intranet.cic.services.impl;

import com.intranet.cic.execeptions.IntranetException;
import com.intranet.cic.services.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload-dir:/uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://192.168.120.10}")
    private String baseUrl;

    private static final List<String> ALLOWED_DOCUMENT_TYPES = List.of(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv"
    );
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "jpg", "jpeg", "png", "gif", "webp", "svg"
    );

    // ── Store ──────────────────────────────────────────────────────────────────

    public String storeDocument(MultipartFile file) {
        validateExtension(file, ALLOWED_DOCUMENT_TYPES, "document");
        return store(file, "documents");
    }

    public String storeImage(MultipartFile file) {
        validateExtension(file, ALLOWED_IMAGE_TYPES, "image");
        return store(file, "images");
    }

    private String store(MultipartFile file, String folder) {
        try {
            Path uploadPath = Paths.get(uploadDir, folder);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return URL pointing to nginx /uploads/ endpoint
            return baseUrl + "/uploads/" + folder + "/" + fileName;

        } catch (IOException e) {
            log.error("Failed to store file locally", e);
            throw new IntranetException("Failed to store file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── Delete ─────────────────────────────────────────────────────────────────

    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isBlank()) return;

            // Extract path after /uploads/
            String filePath = fileUrl.substring(fileUrl.indexOf("/uploads/"));
            Path path = Paths.get(uploadDir + filePath.replace("/uploads", ""));
            Files.deleteIfExists(path);

        } catch (IOException e) {
            log.warn("Failed to delete file: {}", fileUrl, e);
        }
    }

    // ── Resolve ────────────────────────────────────────────────────────────────

    public Path resolveFilePath(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new IntranetException("File URL is empty", HttpStatus.BAD_REQUEST);
        }

        String filePath = fileUrl.substring(fileUrl.indexOf("/uploads/"));
        Path path = Paths.get(uploadDir + filePath.replace("/uploads", ""));

        if (!Files.exists(path)) {
            throw new IntranetException("File not found", HttpStatus.NOT_FOUND);
        }

        return path;
    }

    // ── Validation ─────────────────────────────────────────────────────────────

    private void validateExtension(MultipartFile file, List<String> allowedTypes, String fileCategory) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IntranetException("Invalid file name", HttpStatus.BAD_REQUEST);
        }
        String extension = originalFilename
                .substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        if (!allowedTypes.contains(extension)) {
            throw new IntranetException(
                    "Invalid " + fileCategory + " type. Allowed: " + allowedTypes,
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}