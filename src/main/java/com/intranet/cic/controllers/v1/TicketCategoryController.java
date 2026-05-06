package com.intranet.cic.controllers.v1;

import com.intranet.cic.entities.TicketCategory;
import com.intranet.cic.services.TicketCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TicketCategoryController {

    private final TicketCategoryService ticketCategoryService;

    // ── Public — employee fetches filtered categories ─────────────────────
    @GetMapping("/api/public/ticket-categories")
    public ResponseEntity<List<TicketCategory>> getCategories(
            @RequestParam String segment,
            @RequestParam(required = false) String department
    ) {
        return ResponseEntity.ok(
                ticketCategoryService.getApplicableCategories(segment, department)
        );
    }

    // ── Admin — get all ───────────────────────────────────────────────────
    @GetMapping("/api/admin/ticket-categories")
    public ResponseEntity<List<TicketCategory>> getAllCategories() {
        return ResponseEntity.ok(ticketCategoryService.getAllCategories());
    }

    // ── Admin — create ────────────────────────────────────────────────────
    @PostMapping("/api/admin/ticket-categories")
    public ResponseEntity<TicketCategory> createCategory(
            @RequestBody TicketCategory category
    ) {
        return ResponseEntity.ok(ticketCategoryService.createCategory(category));
    }

    // ── Admin — update ────────────────────────────────────────────────────
    @PutMapping("/api/admin/ticket-categories/{id}")
    public ResponseEntity<TicketCategory> updateCategory(
            @PathVariable Long id,
            @RequestBody TicketCategory body
    ) {
        return ResponseEntity.ok(ticketCategoryService.updateCategory(id, body));
    }

    // ── Admin — delete ────────────────────────────────────────────────────
    @DeleteMapping("/api/admin/ticket-categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        ticketCategoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
