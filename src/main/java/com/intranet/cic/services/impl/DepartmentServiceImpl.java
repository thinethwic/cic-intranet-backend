package com.intranet.cic.services.impl;

import com.intranet.cic.dtos.DepartmentDTO;
import com.intranet.cic.entities.Department;
import com.intranet.cic.entities.types.Segment;
import com.intranet.cic.execeptions.IntranetException;
import com.intranet.cic.repositories.DepartmentRepository;
import com.intranet.cic.services.DepartmentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final ModelMapper modelMapper;

    // ── Mapping ───────────────────────────────────────────────────────────────

    private DepartmentDTO.Response toResponse(Department d) {
        return modelMapper.map(d, DepartmentDTO.Response.class);
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Override
    public Page<DepartmentDTO.Response> getAll(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(
                    page, size,
                    Sort.by("segment").ascending().and(Sort.by("name").ascending())
            );
            return departmentRepository.findAll(pageable)
                    .map(this::toResponse);
        } catch (Exception e) {
            log.error("Failed to fetch paginated departments", e);
            throw new IntranetException("Failed to fetch departments", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<DepartmentDTO.Response> getBySegment(Segment segment) {
        try {
            return departmentRepository.findBySegmentOrderByNameAsc(segment)
                    .stream()
                    .map(this::toResponse)
                    .toList();
        } catch (Exception e) {
            log.error("Failed to fetch departments for segment: {}", segment, e);
            throw new IntranetException("Failed to fetch departments", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public DepartmentDTO.Response getById(Long id) {
        try {
            return departmentRepository.findById(id)
                    .map(this::toResponse)
                    .orElseThrow(() -> new IntranetException(
                            "Department not found with id: " + id, HttpStatus.NOT_FOUND));
        } catch (IntranetException e) {
            log.warn("Department not found with id: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch department with id: {}", id, e);
            throw new IntranetException("Failed to fetch department", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public DepartmentDTO.Response create(DepartmentDTO.Request req) {
        try {
            validateUnique(req, null);

            Department dept = new Department();
            dept.setName(req.getName().trim());
            dept.setCode(req.getCode().trim().toUpperCase());
            dept.setSegment(req.getSegment());

            return toResponse(departmentRepository.save(dept));
        } catch (IntranetException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create department: {}", req.getName(), e);
            throw new IntranetException("Failed to create department", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public DepartmentDTO.Response update(Long id, DepartmentDTO.Request req) {
        try {
            Department dept = departmentRepository.findById(id)
                    .orElseThrow(() -> new IntranetException(
                            "Department not found with id: " + id, HttpStatus.NOT_FOUND));

            validateUnique(req, id);

            dept.setName(req.getName().trim());
            dept.setCode(req.getCode().trim().toUpperCase());
            dept.setSegment(req.getSegment());

            return toResponse(departmentRepository.save(dept));
        } catch (IntranetException e) {
            log.warn("Business error updating department id: {}", id, e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to update department with id: {}", id, e);
            throw new IntranetException("Failed to update department", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        try {
            Department dept = departmentRepository.findById(id)
                    .orElseThrow(() -> new IntranetException(
                            "Department not found with id: " + id, HttpStatus.NOT_FOUND));
            departmentRepository.delete(dept);
        } catch (IntranetException e) {
            log.warn("Department not found with id: {} to delete", id);
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete department with id: {}", id, e);
            throw new IntranetException("Failed to delete department", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private void validateUnique(DepartmentDTO.Request req, Long excludeId) {
        boolean codeTaken = excludeId == null
                ? departmentRepository.existsByCodeIgnoreCase(req.getCode())
                : departmentRepository.existsByCodeIgnoreCaseAndIdNot(req.getCode(), excludeId);

        if (codeTaken) {
            throw new IntranetException(
                    "Department code '" + req.getCode() + "' is already in use",
                    HttpStatus.CONFLICT);
        }

        boolean nameTaken = excludeId == null
                ? departmentRepository.existsByNameIgnoreCaseAndSegment(req.getName(), req.getSegment())
                : departmentRepository.existsByNameIgnoreCaseAndSegmentAndIdNot(req.getName(), req.getSegment(), excludeId);

        if (nameTaken) {
            throw new IntranetException(
                    "A department named '" + req.getName() + "' already exists in this segment",
                    HttpStatus.CONFLICT);
        }
    }
}