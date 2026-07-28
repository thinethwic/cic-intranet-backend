package com.intranet.cic.controllers.v1;

import com.intranet.cic.controllers.AbstractController;
import com.intranet.cic.dtos.NavItemDTO;
import com.intranet.cic.entities.NavItem;
import com.intranet.cic.services.NavItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.intranet.cic.constants.UserRoles.ROLE_SUPER_ADMIN;

@RestController
@RequestMapping(path = "/api/v1/nav-items")
@RequiredArgsConstructor
@Slf4j
public class NavItemController extends AbstractController {

    private final NavItemService navItemService;

    // Public — the navbar reads this without auth.
    @GetMapping
    public ResponseEntity<List<NavItem>> getTree() {
        return sendOkResponse(navItemService.getTree());
    }

    @PostMapping
    @PreAuthorize("hasRole('" + ROLE_SUPER_ADMIN + "')")
    public ResponseEntity<NavItem> createNavItem(@Valid @RequestBody NavItemDTO navItemDTO) {
        return sendCreatedResponse(navItemService.createNavItem(navItemDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('" + ROLE_SUPER_ADMIN + "')")
    public ResponseEntity<NavItem> updateNavItem(
            @PathVariable Long id,
            @Valid @RequestBody NavItemDTO navItemDTO) {
        return sendOkResponse(navItemService.updateNavItem(id, navItemDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('" + ROLE_SUPER_ADMIN + "')")
    public ResponseEntity<Void> deleteNavItem(@PathVariable Long id) {
        navItemService.deleteNavItem(id);
        return sendNoContentResponse();
    }
}
