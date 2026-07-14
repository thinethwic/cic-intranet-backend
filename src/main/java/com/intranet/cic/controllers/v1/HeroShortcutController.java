package com.intranet.cic.controllers.v1;

import com.intranet.cic.controllers.AbstractController;
import com.intranet.cic.dtos.HeroShortcutDTO;
import com.intranet.cic.services.HeroShortcutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.intranet.cic.constants.UserRoles.ROLE_SUPER_ADMIN;

@RestController
@RequestMapping(path = "/api/v1/hero-shortcuts")
@RequiredArgsConstructor
@Slf4j
public class HeroShortcutController extends AbstractController {

    private final HeroShortcutService heroShortcutService;

    @PostMapping
    @PreAuthorize("hasRole('" + ROLE_SUPER_ADMIN + "')")
    public ResponseEntity<HeroShortcutDTO.Response> create(
            @Valid @RequestBody HeroShortcutDTO.Request request) {
        return sendCreatedResponse(heroShortcutService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('" + ROLE_SUPER_ADMIN + "')")
    public ResponseEntity<HeroShortcutDTO.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody HeroShortcutDTO.Request request) {
        return sendOkResponse(heroShortcutService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('" + ROLE_SUPER_ADMIN + "')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        heroShortcutService.delete(id);
        return sendNoContentResponse();
    }
}
