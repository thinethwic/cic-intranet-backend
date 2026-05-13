package com.intranet.cic.services.impl;

import com.intranet.cic.dtos.EventDTO;
import com.intranet.cic.entities.Event;
import com.intranet.cic.entities.User;
import com.intranet.cic.execeptions.IntranetException;
import com.intranet.cic.repositories.EventRepository;
import com.intranet.cic.repositories.UserRepository;
import com.intranet.cic.services.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    @Override
    public Page<Event> getAllEvents(Pageable pageable) {
        try{
            Pageable sorted = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt")  // ← newest first
            );
            return eventRepository.findAll(sorted);
        } catch (Exception exception){
            log.error("Failed to get all events", exception);
            throw new IntranetException("Failed to get all events", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Event getEventById(Long id) {
        try{
            return eventRepository.findById(id)
                    .orElseThrow(()-> new IntranetException("Event Not found", HttpStatus.NOT_FOUND)
                    );
        } catch (IntranetException intranetException) {

            log.warn("Event not found with id: {} to fetch", id, intranetException);
            throw new IntranetException("Event Not found", HttpStatus.NOT_FOUND);
        } catch (Exception exception) {
            log.error("Error getting event", exception);
            throw new IntranetException("Failed to get event", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Event createEvent(EventDTO eventDTO) {
        try{
            User user = userRepository.findById(eventDTO.getUserId())
                    .orElseThrow(() -> new IntranetException("User Not found", HttpStatus.NOT_FOUND));

            Event event = modelMapper.map(eventDTO, Event.class);
            event.setUser(user);

            return eventRepository.save(event);
        } catch (Exception exception){
            log.error("Failed to create event", exception);
            throw new IntranetException("Failed to create event", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Event updateEventById(Long id, EventDTO eventUpdateDTO) {
        try {
            Event event = eventRepository.findById(id)
                    .orElseThrow(() -> new IntranetException("Event Not found", HttpStatus.NOT_FOUND));

            User user = userRepository.findById(eventUpdateDTO.getUserId())
                    .orElseThrow(() -> new IntranetException("User Not found", HttpStatus.NOT_FOUND));

            modelMapper.map(eventUpdateDTO, event);  // ← map INTO existing event, not new object
            event.setUser(user);

            return eventRepository.save(event);

        } catch (IntranetException intranetException) {
            log.warn("Event not found with id: {} to fetch", id, intranetException);
            throw new IntranetException("Event Not found", HttpStatus.NOT_FOUND);
        } catch (Exception exception) {
            log.error("Error updating Event", exception);
            throw new IntranetException("Failed to update Event", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteEvent(Long id) {
        try{
            Event event = eventRepository.findById(id)
                    .orElseThrow(()-> new IntranetException("Event Not found", HttpStatus.NOT_FOUND)
                    );
            eventRepository.delete(event);
        }  catch (IntranetException intranetException) {

            log.warn("Event not found with id: {} to fetch", id, intranetException);
            throw new IntranetException("Event Not found", HttpStatus.NOT_FOUND);

        } catch (Exception exception) {

            log.error("Error updating Event", exception);
            throw new IntranetException("Failed to update Event", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
