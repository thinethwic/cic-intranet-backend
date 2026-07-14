package com.intranet.cic.services.impl;

import com.intranet.cic.dtos.HeroShortcutDTO;
import com.intranet.cic.entities.HeroShortcut;
import com.intranet.cic.entities.HeroShortcutGroup;
import com.intranet.cic.execeptions.IntranetException;
import com.intranet.cic.repositories.HeroShortcutGroupRepository;
import com.intranet.cic.repositories.HeroShortcutRepository;
import com.intranet.cic.services.HeroShortcutService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class HeroShortcutServiceImpl implements HeroShortcutService {

    private final HeroShortcutRepository shortcutRepository;
    private final HeroShortcutGroupRepository groupRepository;

    // ── Mapping ───────────────────────────────────────────────────────────────

    private HeroShortcutDTO.Response toResponse(HeroShortcut s) {
        return new HeroShortcutDTO.Response(
                s.getId(), s.getLabel(), s.getUrl(), s.getIconName(),
                s.getColor(), s.getGroup().getId(), s.getSortOrder()
        );
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public HeroShortcutDTO.Response create(HeroShortcutDTO.Request req) {
        try {
            HeroShortcutGroup group = groupRepository.findById(req.getGroupId())
                    .orElseThrow(() -> new IntranetException(
                            "Group not found with id: " + req.getGroupId(), HttpStatus.NOT_FOUND));

            HeroShortcut shortcut = new HeroShortcut();
            shortcut.setLabel(req.getLabel().trim());
            shortcut.setUrl(req.getUrl().trim());
            shortcut.setIconName(req.getIconName().trim());
            shortcut.setColor(req.getColor());
            shortcut.setSortOrder(req.getSortOrder());
            shortcut.setGroup(group);

            return toResponse(shortcutRepository.save(shortcut));
        } catch (IntranetException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create hero shortcut", e);
            throw new IntranetException("Failed to create shortcut", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public HeroShortcutDTO.Response update(Long id, HeroShortcutDTO.Request req) {
        try {
            HeroShortcut shortcut = shortcutRepository.findById(id)
                    .orElseThrow(() -> new IntranetException(
                            "Shortcut not found with id: " + id, HttpStatus.NOT_FOUND));

            if (!shortcut.getGroup().getId().equals(req.getGroupId())) {
                HeroShortcutGroup group = groupRepository.findById(req.getGroupId())
                        .orElseThrow(() -> new IntranetException(
                                "Group not found with id: " + req.getGroupId(), HttpStatus.NOT_FOUND));
                shortcut.setGroup(group);
            }

            shortcut.setLabel(req.getLabel().trim());
            shortcut.setUrl(req.getUrl().trim());
            shortcut.setIconName(req.getIconName().trim());
            shortcut.setColor(req.getColor());
            shortcut.setSortOrder(req.getSortOrder());

            return toResponse(shortcutRepository.save(shortcut));
        } catch (IntranetException e) {
            log.warn("Business error updating hero shortcut id: {}", id, e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to update hero shortcut id: {}", id, e);
            throw new IntranetException("Failed to update shortcut", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        try {
            HeroShortcut shortcut = shortcutRepository.findById(id)
                    .orElseThrow(() -> new IntranetException(
                            "Shortcut not found with id: " + id, HttpStatus.NOT_FOUND));
            shortcutRepository.delete(shortcut);
        } catch (IntranetException e) {
            log.warn("Shortcut not found with id: {} to delete", id);
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete hero shortcut id: {}", id, e);
            throw new IntranetException("Failed to delete shortcut", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
