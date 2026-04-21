package com.intranet.cic.controllers.v1;

import com.intranet.cic.controllers.AbstractController;
import com.intranet.cic.dtos.AnnouncementDTO;
import com.intranet.cic.entities.Announcement;
import com.intranet.cic.services.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/announcements")
@RequiredArgsConstructor
@Slf4j
public class AnnouncementController extends AbstractController {
    private final AnnouncementService announcementService;

    @GetMapping
    public ResponseEntity<List<Announcement>> getAllAnnouncements() {
        List<Announcement> announcements = announcementService.getAllAnnouncements();
        return sendOkResponse(announcements);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Announcement> getAnnouncementById(@PathVariable Long id) {
        return sendOkResponse(announcementService.getAnnouncementById(id));
    }

    @PostMapping
    public ResponseEntity<Announcement> createAnnouncement(
            @Valid @RequestBody AnnouncementDTO announcementDTO
    ) {
        Announcement announcement = announcementService.createAnnouncement(announcementDTO);
        return sendCreatedResponse(announcement);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Announcement> updateAnnouncement(
            @PathVariable Long id,
            @Valid @RequestBody AnnouncementDTO announcementDTO
    ) {
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
        announcementService.deleteAnnouncement(id);
        return sendNoContentResponse();
    }
}
