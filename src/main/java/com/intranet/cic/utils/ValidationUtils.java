package com.intranet.cic.utils;

import com.intranet.cic.entities.types.Segment;
import com.intranet.cic.execeptions.IntranetException;
import com.intranet.cic.repositories.DepartmentRepository;
import com.intranet.cic.repositories.TicketCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidationUtils {

    private final TicketCategoryRepository ticketCategoryRepository;
    private final DepartmentRepository departmentRepository;

    // ── Ticket Category ───────────────────────────────────────────────────

    /**
     * Ensures no two categories share the same name within the same segment.
     * A null segment means the category applies to all segments, so uniqueness
     * is checked across all null-segment categories to prevent duplicates there too.
     *
     * @param name      the category name to check
     * @param segment   the segment value (maybe null for "all segments")
     * @param excludeId the id of the record being updated, or null on create
     */
    public void validateCategoryUniqueName(String name, String segment, Long excludeId) {
        boolean taken;

        if (segment == null) {
            taken = excludeId == null
                    ? ticketCategoryRepository.existsByNameIgnoreCaseAndSegmentIsNull(name)
                    : ticketCategoryRepository.existsByNameIgnoreCaseAndSegmentIsNullAndIdNot(name, excludeId);
        } else {
            taken = excludeId == null
                    ? ticketCategoryRepository.existsByNameIgnoreCaseAndSegment(name, segment)
                    : ticketCategoryRepository.existsByNameIgnoreCaseAndSegmentAndIdNot(name, segment, excludeId);
        }

        if (taken) {
            String scope = segment != null ? "segment '" + segment + "'" : "the global scope";
            throw new IntranetException(
                    "A category named '" + name + "' already exists in " + scope,
                    HttpStatus.CONFLICT);
        }
    }

    // ── Department ────────────────────────────────────────────────────────

    /**
     * Ensures department codes are globally unique (codes are system identifiers
     * and must not collide regardless of segment).
     *
     * @param code      the department code to check (case-insensitive)
     * @param excludeId the id of the record being updated, or null on create
     */
    public void validateDepartmentCodeUnique(String code, Long excludeId) {
        boolean taken = excludeId == null
                ? departmentRepository.existsByCodeIgnoreCase(code)
                : departmentRepository.existsByCodeIgnoreCaseAndIdNot(code, excludeId);

        if (taken) {
            throw new IntranetException(
                    "Department code '" + code + "' is already in use",
                    HttpStatus.CONFLICT);
        }
    }

    /**
     * Ensures no two departments share the same name within the same segment.
     *
     * @param name      the department name to check
     * @param segment   the segment the department belongs to
     * @param excludeId the id of the record being updated, or null on create
     */
    public void validateDepartmentNameUnique(String name, Segment segment, Long excludeId) {
        boolean taken = excludeId == null
                ? departmentRepository.existsByNameIgnoreCaseAndSegment(name, segment)
                : departmentRepository.existsByNameIgnoreCaseAndSegmentAndIdNot(name, segment, excludeId);

        if (taken) {
            throw new IntranetException(
                    "A department named '" + name + "' already exists in this segment",
                    HttpStatus.CONFLICT);
        }
    }
}