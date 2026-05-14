package com.intranet.cic.services.impl;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
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

    @Value("${gcs.bucket-name}")
    private String bucketName;

    private final Storage storage;

    public FileStorageServiceImpl(Storage storage) {
        this.storage = storage;
    }

    private static final List<String> ALLOWED_DOCUMENT_TYPES = List.of(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv"
    );
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "jpg", "jpeg", "png", "gif", "webp", "svg"
    );

    // ── Store ──────────────────────────────────────────────────────────────────

    public String storeDocument(MultipartFile file) {
        validateExtension(file, ALLOWED_DOCUMENT_TYPES, "document");
        return store(file, "documents/");
    }

    public String storeImage(MultipartFile file) {
        validateExtension(file, ALLOWED_IMAGE_TYPES, "image");
        return store(file, "images/");
    }

    private String store(MultipartFile file, String folder) {
        try {
            String fileName = folder + UUID.randomUUID() + "_" + file.getOriginalFilename();

            BlobId blobId = BlobId.of(bucketName, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();

            storage.create(blobInfo, file.getBytes());

            // Return the public URL
            return "https://storage.googleapis.com/" + bucketName + "/" + fileName;

        } catch (IOException e) {
            log.error("Failed to store file in GCS", e);
            throw new IntranetException("Failed to store file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── Delete ─────────────────────────────────────────────────────────────────

    public void deleteFile(String fileUrl) {
        String prefix = "https://storage.googleapis.com/" + bucketName + "/";
        String blobName = fileUrl.replace(prefix, "");

        BlobId blobId = BlobId.of(bucketName, blobName);
        boolean deleted = storage.delete(blobId);

        if (!deleted) {
            log.warn("File not found in GCS for deletion: {}", fileUrl);
        }
    }

    // ── Download / Resolve ─────────────────────────────────────────────────────

    public Path resolveFilePath(String fileUrl) {
        try {
            String prefix = "https://storage.googleapis.com/" + bucketName + "/";
            String blobName = fileUrl.replace(prefix, "");

            Blob blob = storage.get(BlobId.of(bucketName, blobName));
            if (blob == null) {
                throw new IntranetException("File not found", HttpStatus.NOT_FOUND);
            }

            // Download to a temp file and return its path
            String extension = blobName.substring(blobName.lastIndexOf('.'));
            Path tempFile = Files.createTempFile("gcs-download-", extension);
            blob.downloadTo(tempFile);

            return tempFile;

        } catch (IOException e) {
            log.error("Failed to resolve file from GCS", e);
            throw new IntranetException("Failed to download file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
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