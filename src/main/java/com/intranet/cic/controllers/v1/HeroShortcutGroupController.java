package com.intranet.cic.controllers.v1;

import com.intranet.cic.controllers.AbstractController;
import com.intranet.cic.dtos.HeroShortcutGroupDTO;
import com.intranet.cic.services.HeroShortcutGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.intranet.cic.constants.UserRoles.ROLE_SUPER_ADMIN;

@RestController
@RequestMapping(path = "/api/v1/hero-shortcut-groups")
@RequiredArgsConstructor
@Slf4j
public class HeroShortcutGroupController extends AbstractController {

    private final HeroShortcutGroupService heroShortcutGroupService;

    // Public — the home page hero section reads this without auth.
    @GetMapping
    public ResponseEntity<List<HeroShortcutGroupDTO.Response>> getAll() {
        return sendOkResponse(heroShortcutGroupService.getAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('" + ROLE_SUPER_ADMIN + "')")
    public ResponseEntity<HeroShortcutGroupDTO.Response> create(
            @Valid @RequestBody HeroShortcutGroupDTO.Request request) {
        return sendCreatedResponse(heroShortcutGroupService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('" + ROLE_SUPER_ADMIN + "')")
    public ResponseEntity<HeroShortcutGroupDTO.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody HeroShortcutGroupDTO.Request request) {
        return sendOkResponse(heroShortcutGroupService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('" + ROLE_SUPER_ADMIN + "')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        heroShortcutGroupService.delete(id);
        return sendNoContentResponse();
    }
}
