package com.intranet.cic.controllers.v1;


import com.intranet.cic.controllers.AbstractController;
import com.intranet.cic.dtos.EventDTO;
import com.intranet.cic.entities.Event;
import com.intranet.cic.services.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/events")
@RequiredArgsConstructor
@Slf4j
public class EventController extends AbstractController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<Page<Event>> getAllEvents(
            @PageableDefault(size = 10, sort = "id") Pageable pageable
    ) {
        Page<Event> events = eventService.getAllEvents(pageable);
        return sendOkResponse(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        return sendOkResponse(eventService.getEventById(id));
    }

    @PostMapping
    public ResponseEntity<Event> createEvent(
            @Valid @RequestBody EventDTO eventDTO
    ) {
        Event event = eventService.createEvent(eventDTO);
        return sendCreatedResponse(event);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventDTO eventDTO
    ) {
        Event event = eventService.updateEventById(id, eventDTO);
        return sendOkResponse(event);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return sendNoContentResponse();
    }
}
