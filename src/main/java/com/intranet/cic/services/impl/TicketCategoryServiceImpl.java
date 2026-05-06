package com.intranet.cic.services.impl;

import com.intranet.cic.entities.TicketCategory;
import com.intranet.cic.repositories.TicketCategoryRepository;
import com.intranet.cic.services.TicketCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketCategoryServiceImpl implements TicketCategoryService {
    private final TicketCategoryRepository ticketCategoryRepository;

    // ── Employee — filtered by segment + department ───────────────────────
    public List<TicketCategory> getApplicableCategories(String segment, String department) {
        return ticketCategoryRepository.findApplicable(
                segment,
                department != null ? department : ""
        );
    }

    // ── Admin — all categories ────────────────────────────────────────────
    public List<TicketCategory> getAllCategories() {
        return ticketCategoryRepository.findAllByOrderByNameAsc();
    }

    // ── Admin — create ────────────────────────────────────────────────────
    public TicketCategory createCategory(TicketCategory category) {
        category.setActive(true);
        return ticketCategoryRepository.save(category);
    }

    // ── Admin — update ────────────────────────────────────────────────────
    public TicketCategory updateCategory(Long id, TicketCategory body) {
        TicketCategory cat = ticketCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        cat.setName(body.getName());
        cat.setSegment(body.getSegment());
        cat.setDepartment(body.getDepartment());
        cat.setActive(body.isActive());
        return ticketCategoryRepository.save(cat);
    }

    // ── Admin — delete ────────────────────────────────────────────────────
    public void deleteCategory(Long id) {
        if (!ticketCategoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found with id: " + id);
        }
        ticketCategoryRepository.deleteById(id);
    }
}
