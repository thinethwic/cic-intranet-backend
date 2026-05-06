package com.intranet.cic.controllers.v1;

import com.intranet.cic.dtos.TicketCommentDTO;
import com.intranet.cic.dtos.TicketDTO;
import com.intranet.cic.entities.Ticket;
import com.intranet.cic.entities.types.TicketCategory;
import com.intranet.cic.entities.types.TicketStatus;
import com.intranet.cic.services.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/tickets")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN')")
public class AdminTicketController {

    private final TicketService ticketService;

    // ─── All tickets (unfiltered) ────────────────────────────────
    @GetMapping
    public ResponseEntity<Page<Ticket>> getAllTickets(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ticketService.getAllTickets(pageable));
    }

    // ─── Filter by status ────────────────────────────────────────
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<TicketDTO>> getByStatus(
            @PathVariable TicketStatus status,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ticketService.getTicketsByStatus(pageable, status));
    }

    // ─── Filter by category ──────────────────────────────────────
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<TicketDTO>> getByCategory(
            @PathVariable TicketCategory category,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ticketService.getTicketsByCategory(pageable, category));
    }

    // ─── Assign ticket to a handler ──────────────────────────────
    @PatchMapping("/{ticketId}/assign/{userId}")
    public ResponseEntity<TicketDTO> assignTicket(
            @PathVariable Long ticketId,
            @PathVariable Long userId) {
        return ResponseEntity.ok(ticketService.assignTicket(ticketId, userId));
    }

    // ─── Update status only ──────────────────────────────────────
    @PatchMapping("/{ticketId}/status")
    public ResponseEntity<TicketDTO> updateStatus(
            @PathVariable Long ticketId,
            @RequestParam TicketStatus status) {
        return ResponseEntity.ok(ticketService.updateTicketStatus(ticketId, status));
    }

    // ─── Full update (title, desc, priority, status, category) ───
    @PatchMapping("/{ticketId}")
    public ResponseEntity<TicketDTO> updateTicket(
            @PathVariable Long ticketId,
            @RequestBody TicketDTO ticketDTO) {
        return ResponseEntity.ok(ticketService.updateTicket(ticketId, ticketDTO));
    }

    // ─── Delete any ticket ───────────────────────────────────────
    @DeleteMapping("/{ticketId}")
    @PreAuthorize("hasRole('ADMIN')")          // handlers can't delete
    public ResponseEntity<Void> deleteTicket(@PathVariable Long ticketId) {
        ticketService.deleteTicket(ticketId);
        return ResponseEntity.noContent().build();
    }

    // ─── Add internal note / public reply ────────────────────────
    @PostMapping("/{ticketId}/comments")
    public ResponseEntity<TicketCommentDTO> addComment(
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketCommentDTO commentDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketService.addComment(ticketId, commentDTO));
    }

    // ─── Get comments for a ticket ───────────────────────────────
    @GetMapping("/{ticketId}/comments")
    public ResponseEntity<Page<TicketCommentDTO>> getComments(
            @PathVariable Long ticketId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ticketService.getCommentsByTicket(pageable, ticketId));
    }
}
