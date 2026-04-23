package com.intranet.cic.services;

import com.intranet.cic.dtos.EventDTO;
import com.intranet.cic.entities.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventService {
    Page<Event> getAllEvents(Pageable pageable);
    Event getEventById(Long id);
    Event createEvent(EventDTO eventDTO);
    Event updateEventById(Long id, EventDTO eventDTO);
    void deleteEvent(Long id);
}
