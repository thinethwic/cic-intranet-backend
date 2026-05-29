package com.intranet.cic.services.impl;

import com.intranet.cic.entities.TicketCategory;
import com.intranet.cic.execeptions.IntranetException;
import com.intranet.cic.repositories.TicketCategoryRepository;
import com.intranet.cic.services.TicketCategoryService;
import com.intranet.cic.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketCategoryServiceImpl implements TicketCategoryService {

    private final TicketCategoryRepository ticketCategoryRepository;
    private final ValidationUtils validationUtils;          // ← injected


    @Override
    public List<TicketCategory> getApplicableCategories(String segment, String department) {
        try {
            if (department != null && !department.isBlank()) {
                return ticketCategoryRepository.findBySegmentAndDepartment(segment, department);
            }
            return ticketCategoryRepository.findBySegmentOnly(segment);
        } catch (Exception e) {
            log.error("Failed to fetch applicable categories for segment: {}, department: {}", segment, department, e);
            throw new IntranetException("Failed to fetch categories", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<TicketCategory> getAllCategories() {
        try {
            return ticketCategoryRepository.findAllByOrderByNameAsc();
        } catch (Exception e) {
            log.error("Failed to fetch all categories", e);
            throw new IntranetException("Failed to fetch categories", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ✅ After — add the setCatCode line
    @Override
    public TicketCategory createCategory(TicketCategory category) {
        try {
            validationUtils.validateCategoryUniqueName(category.getName(), category.getSegment(), null);
            category.setActive(true);
            category.setCatCode(category.getCatCode()); // ✅ explicitly set catCode
            return ticketCategoryRepository.save(category);
        } catch (IntranetException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create category: {}", category.getName(), e);
            throw new IntranetException("Failed to create category", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public TicketCategory updateCategory(Long id, TicketCategory body) {
        try {
            TicketCategory cat = ticketCategoryRepository.findById(id)
                    .orElseThrow(() -> new IntranetException(
                            "Category not found with id: " + id, HttpStatus.NOT_FOUND));

            validationUtils.validateCategoryUniqueName(body.getName(), body.getSegment(), id);

            cat.setName(body.getName());
            cat.setCatCode(body.getCatCode());
            cat.setSegment(body.getSegment());
            cat.setDepartment(body.getDepartment());
            cat.setActive(body.isActive());
            return ticketCategoryRepository.save(cat);
        } catch (IntranetException e) {
            log.warn("Business error updating category id: {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to update category with id: {}", id, e);
            throw new IntranetException("Failed to update category", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteCategory(Long id) {
        try {
            if (!ticketCategoryRepository.existsById(id)) {
                throw new IntranetException(
                        "Category not found with id: " + id, HttpStatus.NOT_FOUND);
            }
            ticketCategoryRepository.deleteById(id);
        } catch (IntranetException e) {
            log.warn("Category not found with id: {} for deletion", id);
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete category with id: {}", id, e);
            throw new IntranetException("Failed to delete category", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}