package com.intranet.cic.controllers.v1;

import com.intranet.cic.dtos.TicketCommentDTO;
import com.intranet.cic.dtos.TicketDTO;
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
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")  // ← all endpoints require login
public class TicketController {

    private final TicketService ticketService;

    // Only AUTHORIZED role can submit tickets
    @PostMapping
    @PreAuthorize("hasRole('AUTHORIZED')")
    public ResponseEntity<TicketDTO> createTicket(@Valid @RequestBody TicketDTO ticketDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketService.createTicket(ticketDTO));
    }

    // Only AUTHORIZED role can view their own tickets
    @GetMapping("/my")
    @PreAuthorize("hasRole('AUTHORIZED')")
    public ResponseEntity<Page<TicketDTO>> getMyTickets(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ticketService.getMyTickets(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('AUTHORIZED')")
    public ResponseEntity<TicketDTO> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('AUTHORIZED')")
    public ResponseEntity<TicketDTO> updateTicket(
            @PathVariable Long id,
            @RequestBody TicketDTO ticketDTO) {
        return ResponseEntity.ok(ticketService.updateTicket(id, ticketDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('AUTHORIZED')")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("hasRole('AUTHORIZED')")
    public ResponseEntity<TicketCommentDTO> addComment(
            @PathVariable Long id,
            @Valid @RequestBody TicketCommentDTO commentDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketService.addComment(id, commentDTO));
    }

    @GetMapping("/{id}/comments")
    @PreAuthorize("hasRole('AUTHORIZED')")
    public ResponseEntity<Page<TicketCommentDTO>> getComments(
            @PathVariable Long id,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ticketService.getCommentsByTicket(pageable, id));
    }
}