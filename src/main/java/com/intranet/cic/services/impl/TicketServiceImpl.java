package com.intranet.cic.services.impl;

import com.intranet.cic.dtos.TicketCommentDTO;
import com.intranet.cic.dtos.TicketDTO;
import com.intranet.cic.entities.Ticket;
import com.intranet.cic.entities.TicketCategory;
import com.intranet.cic.entities.TicketComment;
import com.intranet.cic.entities.User;
import com.intranet.cic.entities.types.Segment;
import com.intranet.cic.entities.types.TicketStatus;
import com.intranet.cic.execeptions.IntranetException;
import com.intranet.cic.repositories.TicketCategoryRepository;
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
//import java.util.stream.Collectors;

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
    private final TicketCategoryRepository ticketCategoryRepository;

//    @Value("${app.superuser.email}")
//    private String superUserEmail;


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

    private String generateTicketNumber(String categoryName, String departmentCode) {
        try {
            int year = Year.now().getValue();

            String dept = (departmentCode == null || departmentCode.isBlank())
                    ? "GEN"
                    : departmentCode.toUpperCase().trim();

            // Look up catCode from DB; fall back to dept-only prefix if not found
            String catCode = null;
            if (categoryName != null && !categoryName.isBlank()) {
                catCode = ticketCategoryRepository
                        .findByNameIgnoreCase(categoryName.trim())
                        .filter(TicketCategory::isActive)
                        .map(TicketCategory::getCatCode)
                        .map(String::toUpperCase)
                        .orElse(null);

                if (catCode == null) {
                    log.warn("No active category found for name='{}' — using dept-only prefix", categoryName);
                }
            }

            String prefix = (catCode != null) ? catCode + "-" + dept : dept;

            String prefixWithYear = prefix + "-" + year;
            long count = ticketRepository.countByTicketNumberStartingWith(prefixWithYear);
            long next  = count + 1;

            String ticketNumber;
            do {
                ticketNumber = String.format("%s-%d-%03d", prefix, year, next);
                next++;
            } while (ticketRepository.existsByTicketNumber(ticketNumber));

            return ticketNumber;

        } catch (IntranetException e) {
            throw e;
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
            ticket.setTicketNumber(generateTicketNumber(
                    ticketDTO.getCategory(),
                    ticketDTO.getDepartment()));
            ticket.setStatus(TicketStatus.OPEN);
            ticket.setSubmittedBy(currentUser);
            ticket.setAssignedTo(null);
            ticket.setResolvedAt(null);

            Ticket saved = ticketRepository.save(ticket);


//// Find active admins for this segment + department
//            List<String> adminEmails = userRepository
//                    .findActiveAdminsBySegmentAndDepartment(saved.getSegment(), saved.getDepartment())
//                    .stream()
//                    .map(User::getEmail)
//                    .filter(e -> e != null && !e.isBlank())
//                    .collect(Collectors.toList());
//
//
//            if (adminEmails.isEmpty()) {
//                log.warn("No admin found for segment={}, department={} — falling back to superuser",
//                        saved.getSegment(), saved.getDepartment());
//                adminEmails = Arrays.stream(superUserEmail.split(","))
//                        .map(String::trim)
//                        .filter(e -> !e.isBlank())
//                        .collect(Collectors.toList());
//            }

            emailService.sendNewTicketNotification(
                    saved.getTicketNumber(),
                    saved.getTitle(),
                    saved.getDescription(),
                    saved.getPriority().name(),
                    saved.getSegment() != null ? saved.getSegment().name() : "N/A",
                    saved.getDepartment(),
                    saved.getCreatedAt(),
                    currentUser.getName(),
                    currentUser.getEmail()
//                    adminEmails
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
    public Page<TicketDTO> getTicketsByCategory(Pageable pageable, String category) {
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

    /**
     * Ticket visibility routing for ADMIN role.
     *
     * Two rules depending on the admin's department:
     *
     *   1. IT admin  (department = "IT", any segment)
     *      → returns ALL tickets whose category = 'IT', across every segment.
     *        The IT function is centralised; IT admins are not segment-scoped.
     *
     *   2. Non-IT admin  (HR, Finance, Facilities, Other…)
     *      → returns tickets where BOTH segment AND department match the
     *        admin's own segment and department.
     *
     * SUPER_ADMIN does not call this method — they use getAllTickets().
     */
    @Transactional(readOnly = true)
    public Page<Ticket> getTicketsByCurrentAdminScope(Pageable pageable) {
        try {
            String principal = SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getName();

            User admin = userRepository.findByUsername(principal)
                    .orElseThrow(() -> new IntranetException(
                            "Admin user not found",
                            HttpStatus.UNAUTHORIZED));

            String department = admin.getDepartment();
            Segment segment   = admin.getSegment();

            // ── Rule 1: IT admin — cross-segment, category-scoped ──────────
            if (department != null
                    && department.trim().equalsIgnoreCase("IT")) {
                log.debug("IT admin {} — returning IT-category tickets from all segments",
                        principal);
                return ticketRepository.findByCategory("IT", pageable);
            }

            // ── Rule 2: Non-IT admin — segment + department scoped ─────────
            if (segment == null) {
                log.warn("Admin {} has no segment set — returning empty page", principal);
                return Page.empty(pageable);
            }

            if (department != null && !department.isBlank()) {
                log.debug("Admin {} — returning tickets for segment={}, department={}",
                        principal, segment, department);
                return ticketRepository
                        .findBySegmentAndDepartmentIgnoreCase(segment, department, pageable);
            }

            // Fallback: segment only (no department set on admin)
            log.debug("Admin {} — no department set, returning all tickets for segment={}",
                    principal, segment);
            return ticketRepository.findBySegment(segment, pageable);

        } catch (IntranetException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch tickets by admin scope", e);
            throw new IntranetException(
                    "Failed to fetch tickets by scope",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * @deprecated Replaced by {@link #getTicketsByCurrentAdminScope(Pageable)}.
     * Kept for backwards compatibility; delegates to the new method.
     */
    @Override
    @Deprecated
    @Transactional(readOnly = true)
    public Page<Ticket> getTicketsByCurrentAdminSegment(Pageable pageable) {
        return getTicketsByCurrentAdminScope(pageable);
    }
}