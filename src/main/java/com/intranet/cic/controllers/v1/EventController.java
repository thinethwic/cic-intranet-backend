package com.intranet.cic.controllers.v1;


import com.intranet.cic.controllers.AbstractController;
import com.intranet.cic.dtos.EventDTO;
import com.intranet.cic.entities.Event;
import com.intranet.cic.services.EventService;
import com.intranet.cic.services.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "/api/v1/events")
@RequiredArgsConstructor
@Slf4j
public class EventController extends AbstractController {

    private final EventService eventService;
    private final FileStorageService fileStorageService; // ✅ add this

    @GetMapping
    public ResponseEntity<Page<Event>> getAllEvents(
            @PageableDefault(size = 10, sort = "id") Pageable pageable
    ) {
        return sendOkResponse(eventService.getAllEvents(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        return sendOkResponse(eventService.getEventById(id));
    }

    // ✅ multipart — image optional
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Event> createEvent(
            @RequestPart("data") @Valid EventDTO eventDTO,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        if (image != null && !image.isEmpty()) {
            eventDTO.setImage(fileStorageService.storeImage(image));
        }
        return sendCreatedResponse(eventService.createEvent(eventDTO));
    }

    // ✅ multipart — image optional
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Event> updateEvent(
            @PathVariable Long id,
            @RequestPart("data") @Valid EventDTO eventDTO,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        if (image != null && !image.isEmpty()) {
            Event existing = eventService.getEventById(id);
            if (existing.getImage() != null) {
                fileStorageService.deleteFile(existing.getImage());
            }
            eventDTO.setImage(fileStorageService.storeImage(image));
        }
        return sendOkResponse(eventService.updateEventById(id, eventDTO));
    }

    // ✅ delete physical image file too
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        Event existing = eventService.getEventById(id);
        if (existing.getImage() != null) {
            fileStorageService.deleteFile(existing.getImage());
        }
        eventService.deleteEvent(id);
        return sendNoContentResponse();
    }
}