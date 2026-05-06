package com.intranet.cic.services;

import com.intranet.cic.entities.TicketCategory;

import java.util.List;

public interface TicketCategoryService {
    List<TicketCategory> getApplicableCategories(String segment, String department);
    List<TicketCategory> getAllCategories();
    TicketCategory createCategory(TicketCategory category);
    TicketCategory updateCategory(Long id, TicketCategory body);
    void deleteCategory(Long id);

}
