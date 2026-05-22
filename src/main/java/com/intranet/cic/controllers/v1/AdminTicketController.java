package com.intranet.cic.controllers.v1;

import com.intranet.cic.controllers.AbstractController;
import com.intranet.cic.dtos.TicketCommentDTO;
import com.intranet.cic.dtos.TicketDTO;
import com.intranet.cic.entities.Ticket;
import com.intranet.cic.entities.types.TicketStatus;
import com.intranet.cic.services.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.intranet.cic.constants.UserRoles.*;

@RestController
@RequestMapping("/api/admin/tickets")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminTicketController extends AbstractController {

    private final TicketService ticketService;

    // ─── All tickets (unfiltered) ────────────────────────────────
    @GetMapping
    public ResponseEntity<Page<Ticket>> getAllTickets(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return sendOkResponse(ticketService.getAllTickets(pageable));
    }

    // ─── Get tickets by segment (for ADMIN role) ─────────────────
    @GetMapping("/my-segment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Ticket>> getTicketsBySegment(
            @PageableDefault(size = 100, sort = "createdAt") Pageable pageable) {
        return sendOkResponse(ticketService.getTicketsByCurrentAdminSegment(pageable));
    }

    // ─── Filter by status ────────────────────────────────────────
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<TicketDTO>> getByStatus(
            @PathVariable TicketStatus status,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return sendOkResponse(ticketService.getTicketsByStatus(pageable, status));
    }

    // ─── Filter by category ──────────────────────────────────────
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<TicketDTO>> getByCategory(
            @PathVariable String category,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return sendOkResponse(ticketService.getTicketsByCategory(pageable, category));
    }

    // ─── Assign ticket to a handler ──────────────────────────────
    @PatchMapping("/{ticketId}/assign/{userId}")
    public ResponseEntity<TicketDTO> assignTicket(
            @PathVariable Long ticketId,
            @PathVariable Long userId) {
        return sendOkResponse(ticketService.assignTicket(ticketId, userId));
    }

    // ─── Update status only ──────────────────────────────────────
    @PatchMapping("/{ticketId}/status")
    public ResponseEntity<TicketDTO> updateStatus(
            @PathVariable Long ticketId,
            @RequestParam TicketStatus status) {
        return sendOkResponse(ticketService.updateTicketStatus(ticketId, status));
    }

    // ─── Full update (title, desc, priority, status, category) ───
    @PatchMapping("/{ticketId}")
    public ResponseEntity<TicketDTO> updateTicket(
            @PathVariable Long ticketId,
            @RequestBody TicketDTO ticketDTO) {
        return sendOkResponse(ticketService.updateTicket(ticketId, ticketDTO));
    }

    // ─── Delete any ticket ───────────────────────────────────────
    @DeleteMapping("/{ticketId}")
    @PreAuthorize("hasAnyRole('" + ROLE_SUPER_ADMIN + "','" + ROLE_ADMIN + "')")          // handlers can't delete
    public ResponseEntity<Void> deleteTicket(@PathVariable Long ticketId) {
        ticketService.deleteTicket(ticketId);
        return sendNoContentResponse();
    }

    // ─── Add internal note / public reply ────────────────────────
    @PostMapping("/{ticketId}/comments")
    public ResponseEntity<TicketCommentDTO> addComment(
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketCommentDTO commentDTO) {
        return sendCreatedResponse(ticketService.addComment(ticketId, commentDTO));
    }

    // ─── Get comments for a ticket ───────────────────────────────
    @GetMapping("/{ticketId}/comments")
    public ResponseEntity<Page<TicketCommentDTO>> getComments(
            @PathVariable Long ticketId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return sendOkResponse(ticketService.getCommentsByTicket(pageable, ticketId));
    }
}
