package com.intranet.cic.controllers.v1;

import com.intranet.cic.controllers.AbstractController;
import com.intranet.cic.dtos.AnnouncementDTO;
import com.intranet.cic.entities.Announcement;
import com.intranet.cic.services.AnnouncementService;
import com.intranet.cic.services.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/announcements")
@RequiredArgsConstructor
@Slf4j
public class AnnouncementController extends AbstractController {
    private final AnnouncementService announcementService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<List<Announcement>> getAllAnnouncements() {
        List<Announcement> announcements = announcementService.getAllAnnouncements();
        return sendOkResponse(announcements);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Announcement> getAnnouncementById(@PathVariable Long id) {
        return sendOkResponse(announcementService.getAnnouncementById(id));
    }

    // ✅ multipart — image optional
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Announcement> createAnnouncement(
            @RequestPart("data") @Valid AnnouncementDTO announcementDTO,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        if (image != null && !image.isEmpty()) {
            announcementDTO.setImage(fileStorageService.storeImage(image));
        }
        Announcement announcement = announcementService.createAnnouncement(announcementDTO);
        return sendCreatedResponse(announcement);
    }

    // ✅ multipart — image optional
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Announcement> updateAnnouncement(
            @PathVariable Long id,
            @RequestPart("data") @Valid AnnouncementDTO announcementDTO,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        if (image != null && !image.isEmpty()) {
            Announcement existing = announcementService.getAnnouncementById(id);
            if (existing.getImage() != null) {
                fileStorageService.deleteFile(existing.getImage());
            }
            announcementDTO.setImage(fileStorageService.storeImage(image));
        }
        Announcement announcement = announcementService.updateAnnouncement(id, announcementDTO);
        return sendOkResponse(announcement);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Announcement> markAsRead(@PathVariable Long id) {
        Announcement announcement = announcementService.markAsRead(id);
        return sendOkResponse(announcement);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable Long id) {
        Announcement existing = announcementService.getAnnouncementById(id);
        if (existing.getImage() != null) {
            fileStorageService.deleteFile(existing.getImage());
        }
        announcementService.deleteAnnouncement(id);
        return sendNoContentResponse();
    }
}
