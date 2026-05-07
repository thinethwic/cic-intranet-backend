package com.intranet.cic.controllers.v1;

import com.intranet.cic.controllers.AbstractController;
import com.intranet.cic.entities.TicketCategory;
import com.intranet.cic.services.TicketCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TicketCategoryController extends AbstractController {

    private final TicketCategoryService ticketCategoryService;

    // ── Public — employee fetches filtered categories ─────────────────────
    @GetMapping("/public/ticket-categories")
    public ResponseEntity<List<TicketCategory>> getCategories(
            @RequestParam String segment,
            @RequestParam(required = false) String department
    ) {
        return sendOkResponse(
                ticketCategoryService.getApplicableCategories(segment, department)
        );
    }

    // ── Admin — get all ───────────────────────────────────────────────────
    @GetMapping("/admin/ticket-categories")
    public ResponseEntity<List<TicketCategory>> getAllCategories() {
        return sendOkResponse(ticketCategoryService.getAllCategories());
    }

    // ── Admin — create ────────────────────────────────────────────────────
    @PostMapping("/admin/ticket-categories")
    public ResponseEntity<TicketCategory> createCategory(
            @RequestBody TicketCategory category
    ) {
        return sendOkResponse(ticketCategoryService.createCategory(category));
    }

    // ── Admin — update ────────────────────────────────────────────────────
    @PutMapping("/admin/ticket-categories/{id}")
    public ResponseEntity<TicketCategory> updateCategory(
            @PathVariable Long id,
            @RequestBody TicketCategory body
    ) {
        return sendOkResponse(ticketCategoryService.updateCategory(id, body));
    }

    // ── Admin — delete ────────────────────────────────────────────────────
    @DeleteMapping("/admin/ticket-categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        ticketCategoryService.deleteCategory(id);
        return sendNoContentResponse();
    }
}
