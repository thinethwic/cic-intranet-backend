package com.intranet.cic.services.impl;

import com.intranet.cic.entities.types.FileType;
import com.intranet.cic.execeptions.IntranetException;
import com.intranet.cic.services.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {
    @Value("${file.upload-dir.documents}")
    private String documentUploadDir;

    @Value("${file.upload-dir.images}")
    private String imageUploadDir;

    // ── Allowed extensions ─────────────────────────────────────────────────────
    private static final List<String> ALLOWED_DOCUMENT_TYPES = List.of(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv"
    );

    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "jpg", "jpeg", "png", "gif", "webp", "svg"
    );

    // ── Store ──────────────────────────────────────────────────────────────────

    public String storeDocument(MultipartFile file) {
        validateExtension(file, ALLOWED_DOCUMENT_TYPES, "document");
        return store(file, documentUploadDir, "/uploads/documents/");
    }

    public String storeImage(MultipartFile file) {
        validateExtension(file, ALLOWED_IMAGE_TYPES, "image");
        return store(file, imageUploadDir, "/uploads/images/");
    }

    private String store(MultipartFile file, String uploadDir, String urlPrefix) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return urlPrefix + fileName;    // URL saved to DB
        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new IntranetException("Failed to store file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── Delete ─────────────────────────────────────────────────────────────────

    public void deleteFile(String fileUrl) {
        try {
            Path filePath = resolveFilePath(fileUrl);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileUrl, e);
            throw new IntranetException("Failed to delete file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── Download ───────────────────────────────────────────────────────────────

    public Path resolveFilePath(String fileUrl) {
        if (fileUrl.contains("/uploads/documents/")) {
            String fileName = fileUrl.replace("/uploads/documents/", "");
            return Paths.get(documentUploadDir).resolve(fileName).normalize();
        } else if (fileUrl.contains("/uploads/images/")) {
            String fileName = fileUrl.replace("/uploads/images/", "");
            return Paths.get(imageUploadDir).resolve(fileName).normalize();
        }
        throw new IntranetException("Unknown file URL format", HttpStatus.BAD_REQUEST);
    }

    // ── Validation ─────────────────────────────────────────────────────────────

    private void validateExtension(MultipartFile file, List<String> allowedTypes, String fileCategory) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IntranetException("Invalid file name", HttpStatus.BAD_REQUEST);
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        if (!allowedTypes.contains(extension)) {
            throw new IntranetException(
                    "Invalid " + fileCategory + " type. Allowed: " + allowedTypes,
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}
