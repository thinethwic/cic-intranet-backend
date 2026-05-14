package com.intranet.cic.services.impl;

import com.intranet.cic.dtos.TicketCommentDTO;
import com.intranet.cic.dtos.TicketDTO;
import com.intranet.cic.entities.Ticket;
import com.intranet.cic.entities.TicketComment;
import com.intranet.cic.entities.User;
import com.intranet.cic.entities.types.Segment;
import com.intranet.cic.entities.types.TicketCategory;
import com.intranet.cic.entities.types.TicketStatus;
import com.intranet.cic.execeptions.IntranetException;
import com.intranet.cic.repositories.TicketCommentRepository;
import com.intranet.cic.repositories.TicketRepository;
import com.intranet.cic.repositories.UserRepository;
import com.intranet.cic.services.EmailService;
import com.intranet.cic.services.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final TicketCommentRepository ticketCommentRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final EmailService emailService;


    private User getCurrentUser() {
        try {
            String username = SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getName();
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new IntranetException(
                            "Authenticated user not found: " + username, HttpStatus.UNAUTHORIZED));
        } catch (IntranetException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to resolve authenticated user", e);
            throw new IntranetException("Failed to resolve authenticated user", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String generateTicketNumber() {
        try {
            int year = Year.now().getValue();
            long next = ticketRepository.findMaxSequenceForYear(year) + 1;
            return String.format("TKT-%d-%03d", year, next);
        } catch (Exception e) {
            log.error("Failed to generate ticket number", e);
            throw new IntranetException("Failed to generate ticket number", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private TicketCommentDTO toCommentDTO(TicketComment comment) {
        TicketCommentDTO dto = new TicketCommentDTO();
        dto.setId(comment.getId());
        dto.setMessage(comment.getMessage());
        dto.setIsInternal(comment.getIsInternal());
        dto.setCreatedAt(comment.getCreatedAt());

        TicketCommentDTO.CommentedByDTO commentedBy = new TicketCommentDTO.CommentedByDTO();
        commentedBy.setId(comment.getCommentedBy().getId());
        commentedBy.setName(comment.getCommentedBy().getName());
        commentedBy.setRole(comment.getCommentedBy().getRole().name());
        dto.setCommentedBy(commentedBy);

        return dto;
    }

    // ─── Employee ───────────────────────────────────────────────

    @Override
    public TicketDTO createTicket(TicketDTO ticketDTO) {
        try {
            User currentUser = getCurrentUser();

            Ticket ticket = modelMapper.map(ticketDTO, Ticket.class);
            ticket.setId(null);
            ticket.setTicketNumber(generateTicketNumber());
            ticket.setStatus(TicketStatus.OPEN);
            ticket.setSubmittedBy(currentUser);
            ticket.setAssignedTo(null);
            ticket.setResolvedAt(null);

            Ticket saved = ticketRepository.save(ticket);

            // ✅ Notify super admin — runs async, won't slow down the response
            emailService.sendNewTicketNotification(
                    saved.getTicketNumber(),
                    saved.getTitle(),
                    saved.getDescription(),
                    saved.getPriority().name(),
                    saved.getSegment() != null ? saved.getSegment().name() : "N/A",
                    saved.getDepartment(),
                    currentUser.getName(),
                    currentUser.getEmail()
            );

            return modelMapper.map(saved, TicketDTO.class);
        } catch (IntranetException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create ticket", e);
            throw new IntranetException("Failed to create ticket", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketDTO> getMyTickets(Pageable pageable) {
        try {
            User currentUser = getCurrentUser();
            return ticketRepository.findBySubmittedBy(currentUser, pageable)
                    .map(ticket -> modelMapper.map(ticket, TicketDTO.class));
        } catch (IntranetException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch tickets for current user", e);
            throw new IntranetException("Failed to fetch your tickets", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDTO getTicketById(Long id) {
        try {
            Ticket ticket = ticketRepository.findById(id)
                    .orElseThrow(() -> new IntranetException(
                            "Ticket not found with id: " + id, HttpStatus.NOT_FOUND));
            return modelMapper.map(ticket, TicketDTO.class);
        } catch (IntranetException e) {
            log.warn("Ticket not found with id: {}", id, e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch ticket with id: {}", id, e);
            throw new IntranetException("Failed to fetch ticket", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ─── Admin / Handler ────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<Ticket> getAllTickets(Pageable pageable) {
        try {
            return ticketRepository.findAll(pageable)
                    .map(ticket -> modelMapper.map(ticket, Ticket.class));
        } catch (Exception e) {
            log.error("Failed to fetch all tickets", e);
            throw new IntranetException("Failed to fetch all tickets", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketDTO> getTicketsByStatus(Pageable pageable, TicketStatus status) {
        try {
            return ticketRepository.findByStatus(status, pageable)
                    .map(ticket -> modelMapper.map(ticket, TicketDTO.class));
        } catch (Exception e) {
            log.error("Failed to fetch tickets by status: {}", status, e);
            throw new IntranetException("Failed to fetch tickets by status", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketDTO> getTicketsByCategory(Pageable pageable, TicketCategory category) {
        try {
            return ticketRepository.findByCategory(category, pageable)
                    .map(ticket -> modelMapper.map(ticket, TicketDTO.class));
        } catch (Exception e) {
            log.error("Failed to fetch tickets by category: {}", category, e);
            throw new IntranetException("Failed to fetch tickets by category", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public TicketDTO assignTicket(Long ticketId, Long userId) {
        try {
            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new IntranetException(
                            "Ticket not found with id: " + ticketId, HttpStatus.NOT_FOUND));

            User handler = userRepository.findById(userId)
                    .orElseThrow(() -> new IntranetException(
                            "User not found with id: " + userId, HttpStatus.NOT_FOUND));

            ticket.setAssignedTo(handler);
            if (ticket.getStatus() == TicketStatus.OPEN) {
                ticket.setStatus(TicketStatus.IN_PROGRESS);
            }

            return modelMapper.map(ticketRepository.save(ticket), TicketDTO.class);
        } catch (IntranetException e) {
            log.warn("Not found during ticket assignment - ticketId: {}, userId: {}", ticketId, userId, e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to assign ticket id: {} to user id: {}", ticketId, userId, e);
            throw new IntranetException("Failed to assign ticket", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public TicketDTO updateTicketStatus(Long ticketId, TicketStatus newStatus) {
        try {
            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new IntranetException(
                            "Ticket not found with id: " + ticketId, HttpStatus.NOT_FOUND));

            ticket.setStatus(newStatus);
            if (newStatus == TicketStatus.RESOLVED) {
                ticket.setResolvedAt(LocalDateTime.now());
            }

            return modelMapper.map(ticketRepository.save(ticket), TicketDTO.class);
        } catch (IntranetException e) {
            log.warn("Ticket not found with id: {} during status update", ticketId, e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to update status for ticket id: {}", ticketId, e);
            throw new IntranetException("Failed to update ticket status", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ─── Comments ───────────────────────────────────────────────

    @Override
    public TicketCommentDTO addComment(Long ticketId, TicketCommentDTO commentDTO) {
        try {
            User currentUser = getCurrentUser();

            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new IntranetException(
                            "Ticket not found with id: " + ticketId, HttpStatus.NOT_FOUND));

            TicketComment comment = new TicketComment();
            comment.setTicket(ticket);
            comment.setMessage(commentDTO.getMessage());
            comment.setCommentedBy(currentUser);
            comment.setIsInternal(
                    commentDTO.getIsInternal() != null && commentDTO.getIsInternal());

            ticketCommentRepository.save(comment);

            ticket.setUpdatedAt(LocalDateTime.now());
            ticketRepository.save(ticket);

            return toCommentDTO(comment);
        } catch (IntranetException e) {
            log.warn("Not found during add comment - ticketId: {}", ticketId, e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to add comment to ticket id: {}", ticketId, e);
            throw new IntranetException("Failed to add comment", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketCommentDTO> getCommentsByTicket(Pageable pageable, Long ticketId) {
        try {
            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new IntranetException(
                            "Ticket not found with id: " + ticketId, HttpStatus.NOT_FOUND));

            return ticketCommentRepository.findByTicketOrderByCreatedAtAsc(ticket, pageable)
                    .map(this::toCommentDTO);
        } catch (IntranetException e) {
            log.warn("Ticket not found with id: {} during get comments", ticketId, e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch comments for ticket id: {}", ticketId, e);
            throw new IntranetException("Failed to fetch comments", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ─── Shared ─────────────────────────────────────────────────

    @Override
    public TicketDTO updateTicket(Long ticketId, TicketDTO ticketDTO) {
        try {
            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new IntranetException(
                            "Ticket not found with id: " + ticketId, HttpStatus.NOT_FOUND));

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

            if (ticketDTO.getAssignedTo() != null && ticketDTO.getAssignedTo().getId() != null) {
                User assignee = userRepository.findById(ticketDTO.getAssignedTo().getId())
                        .orElseThrow(() -> new IntranetException("User not found", HttpStatus.NOT_FOUND));
                ticket.setAssignedTo(assignee);
            } else if (ticketDTO.getAssignedTo() == null) {
                ticket.setAssignedTo(null);
            }

            return modelMapper.map(ticketRepository.save(ticket), TicketDTO.class);
        } catch (IntranetException e) {
            log.warn("Not found during ticket update - id: {}", ticketId, e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to update ticket id: {}", ticketId, e);
            throw new IntranetException("Failed to update ticket", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteTicket(Long ticketId) {
        try {
            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new IntranetException(
                            "Ticket not found with id: " + ticketId, HttpStatus.NOT_FOUND));
            ticketRepository.delete(ticket);
        } catch (IntranetException e) {
            log.warn("Ticket not found with id: {} to delete", ticketId, e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete ticket id: {}", ticketId, e);
            throw new IntranetException("Failed to delete ticket", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Page<Ticket> getTicketsByCurrentAdminSegment(Pageable pageable) {
        try {
            String principal = SecurityContextHolder.getContext().getAuthentication().getName();

            User admin = userRepository.findByUsername(principal)
                    .orElseThrow(() -> new IntranetException("Admin user not found", HttpStatus.UNAUTHORIZED));

            Segment segment = admin.getSegment();
            if (segment == null) {
                return Page.empty(pageable);
            }

            String department = admin.getDepartment();
            if (department != null && !department.isBlank()) {
                return ticketRepository.findBySegmentAndDepartment(segment, department, pageable);
            }

            return ticketRepository.findBySegment(segment, pageable);
        } catch (IntranetException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch tickets by admin segment", e);
            throw new IntranetException("Failed to fetch tickets by segment", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}