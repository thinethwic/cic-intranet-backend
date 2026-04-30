package com.intranet.cic.services.impl;

import com.intranet.cic.dtos.TicketCommentDTO;
import com.intranet.cic.dtos.TicketDTO;
import com.intranet.cic.entities.Ticket;
import com.intranet.cic.entities.TicketComment;
import com.intranet.cic.entities.User;
import com.intranet.cic.entities.types.TicketCategory;
import com.intranet.cic.entities.types.TicketStatus;
import com.intranet.cic.repositories.TicketCommentRepository;
import com.intranet.cic.repositories.TicketRepository;
import com.intranet.cic.repositories.UserRepository;
import com.intranet.cic.services.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final TicketCommentRepository ticketCommentRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    // ─── Helpers ────────────────────────────────────────────────

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName(); // returns username (set by AuthenticationFilter)

        return userRepository.findByUsername(username) // ← was findByEmail()
                .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + username));
    }

    private String generateTicketNumber() {
        int year = Year.now().getValue();
        // MAX-based: safe under concurrent inserts unlike count()+1
        long next = ticketRepository.findMaxSequenceForYear(year) + 1;
        return String.format("TKT-%d-%03d", year, next);
    }

    // ─── Employee ───────────────────────────────────────────────

    @Override
    public TicketDTO createTicket(TicketDTO ticketDTO) {
        log.info("Creating ticket: {}", ticketDTO.getTitle());

        User currentUser = getCurrentUser();

        Ticket ticket = modelMapper.map(ticketDTO, Ticket.class);
        ticket.setId(null);
        ticket.setTicketNumber(generateTicketNumber());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setSubmittedBy(currentUser);
        ticket.setAssignedTo(null);
        ticket.setResolvedAt(null);

        Ticket saved = ticketRepository.save(ticket);
        log.info("Ticket created: {}", saved.getTicketNumber());

        return modelMapper.map(saved, TicketDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketDTO> getMyTickets(Pageable pageable) {
        User currentUser = getCurrentUser();
        log.info("Fetching tickets for user: {}", currentUser.getEmail());

        return ticketRepository.findBySubmittedBy(currentUser, pageable)
                .map(ticket -> modelMapper.map(ticket, TicketDTO.class));
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDTO getTicketById(Long id) {
        log.info("Fetching ticket by id: {}", id);

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));

        return modelMapper.map(ticket, TicketDTO.class);
    }

    // ─── Admin / Handler ────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<TicketDTO> getAllTickets(Pageable pageable) {
        log.info("Fetching all tickets");

        return ticketRepository.findAll(pageable)
                .map(ticket -> modelMapper.map(ticket, TicketDTO.class));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketDTO> getTicketsByStatus(Pageable pageable, TicketStatus status) {
        log.info("Fetching tickets by status: {}", status);

        return ticketRepository.findByStatus(status, pageable)
                .map(ticket -> modelMapper.map(ticket, TicketDTO.class));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketDTO> getTicketsByCategory(Pageable pageable, TicketCategory category) {
        log.info("Fetching tickets by category: {}", category);

        return ticketRepository.findByCategory(category, pageable)
                .map(ticket -> modelMapper.map(ticket, TicketDTO.class));
    }

    @Override
    public TicketDTO assignTicket(Long ticketId, Long userId) {
        log.info("Assigning ticket {} to user {}", ticketId, userId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        User handler = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        ticket.setAssignedTo(handler);

        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
            log.info("Ticket {} auto-transitioned to IN_PROGRESS", ticket.getTicketNumber());
        }

        return modelMapper.map(ticketRepository.save(ticket), TicketDTO.class);
    }

    @Override
    public TicketDTO updateTicketStatus(Long ticketId, TicketStatus newStatus) {
        log.info("Updating ticket {} status to {}", ticketId, newStatus);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        ticket.setStatus(newStatus);

        if (newStatus == TicketStatus.RESOLVED) {
            ticket.setResolvedAt(LocalDateTime.now());
            log.info("Ticket {} marked as resolved", ticket.getTicketNumber());
        }

        return modelMapper.map(ticketRepository.save(ticket), TicketDTO.class);
    }

    // ─── Comments ───────────────────────────────────────────────

    @Override
    public TicketCommentDTO addComment(Long ticketId, TicketCommentDTO commentDTO) {
        log.info("Adding comment to ticket id: {}", ticketId);

        User currentUser = getCurrentUser();

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        TicketComment comment = new TicketComment();
        comment.setTicket(ticket);
        comment.setMessage(commentDTO.getMessage());
        comment.setCommentedBy(currentUser);
        comment.setIsInternal(
                commentDTO.getIsInternal() != null && commentDTO.getIsInternal()
        );

        return modelMapper.map(ticketCommentRepository.save(comment), TicketCommentDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketCommentDTO> getCommentsByTicket(Pageable pageable, Long ticketId) {
        log.info("Fetching comments for ticket id: {}", ticketId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        return ticketCommentRepository.findByTicketOrderByCreatedAtAsc(ticket, pageable)
                .map(comment -> modelMapper.map(comment, TicketCommentDTO.class));
    }

    // ─── Shared ─────────────────────────────────────────────────

    @Override
    public TicketDTO updateTicket(Long ticketId, TicketDTO ticketDTO) {
        log.info("Updating ticket id: {}", ticketId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        if (ticketDTO.getTitle() != null)       ticket.setTitle(ticketDTO.getTitle());
        if (ticketDTO.getDescription() != null) ticket.setDescription(ticketDTO.getDescription());
        if (ticketDTO.getCategory() != null)    ticket.setCategory(ticketDTO.getCategory());
        if (ticketDTO.getPriority() != null)    ticket.setPriority(ticketDTO.getPriority());
        if (ticketDTO.getStatus() != null) {
            ticket.setStatus(ticketDTO.getStatus());
            if (ticketDTO.getStatus() == TicketStatus.RESOLVED) {
                ticket.setResolvedAt(LocalDateTime.now());
            }
        }

        return modelMapper.map(ticketRepository.save(ticket), TicketDTO.class);
    }

    @Override
    public void deleteTicket(Long ticketId) {
        log.info("Deleting ticket id: {}", ticketId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        ticketRepository.delete(ticket);
        log.info("Ticket {} deleted", ticket.getTicketNumber());
    }
}