package com.intranet.cic.controllers.v1;

import com.intranet.cic.controllers.AbstractController;
import com.intranet.cic.dtos.DepartmentDTO;
import com.intranet.cic.entities.types.Segment;
import com.intranet.cic.services.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.intranet.cic.constants.UserRoles.*;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
@Slf4j
public class DepartmentController extends AbstractController {

    private final DepartmentService departmentService;

    // ── GET /api/v1/departments?page=0&size=10  (admin table, paginated) ─────
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<DepartmentDTO.Response>> getAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return sendOkResponse(departmentService.getAll(page, size));
    }

    // ── GET /api/v1/departments/by-segment/{segment}  (dropdown, flat list) ──
    @GetMapping("/by-segment/{segment}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DepartmentDTO.Response>> getBySegment(
            @PathVariable Segment segment) {
        return sendOkResponse(departmentService.getBySegment(segment));
    }

    // ── GET /api/v1/departments/{id} ─────────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DepartmentDTO.Response> getById(@PathVariable Long id) {
        return sendOkResponse(departmentService.getById(id));
    }

    // ── POST /api/v1/departments ─────────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasAnyRole('" + ROLE_SUPER_ADMIN + "','" + ROLE_ADMIN + "')")
    public ResponseEntity<DepartmentDTO.Response> create(
            @Valid @RequestBody DepartmentDTO.Request request) {
        return sendCreatedResponse(departmentService.create(request));
    }

    // ── PUT /api/v1/departments/{id} ─────────────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('" + ROLE_SUPER_ADMIN + "','" + ROLE_ADMIN + "')")
    public ResponseEntity<DepartmentDTO.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentDTO.Request request) {
        return sendOkResponse(departmentService.update(id, request));
    }

    // ── DELETE /api/v1/departments/{id} ──────────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('" + ROLE_SUPER_ADMIN + "','" + ROLE_ADMIN + "')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return sendNoContentResponse();
    }
}