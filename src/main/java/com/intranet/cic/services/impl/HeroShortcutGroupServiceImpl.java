package com.intranet.cic.services.impl;

import com.intranet.cic.dtos.HeroShortcutDTO;
import com.intranet.cic.dtos.HeroShortcutGroupDTO;
import com.intranet.cic.entities.HeroShortcut;
import com.intranet.cic.entities.HeroShortcutGroup;
import com.intranet.cic.execeptions.IntranetException;
import com.intranet.cic.repositories.HeroShortcutGroupRepository;
import com.intranet.cic.services.HeroShortcutGroupService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HeroShortcutGroupServiceImpl implements HeroShortcutGroupService {

    private final HeroShortcutGroupRepository groupRepository;

    // ── Mapping ───────────────────────────────────────────────────────────────

    private HeroShortcutDTO.Response toShortcutResponse(HeroShortcut s) {
        return new HeroShortcutDTO.Response(
                s.getId(), s.getLabel(), s.getUrl(), s.getIconName(),
                s.getColor(), s.getGroup().getId(), s.getSortOrder()
        );
    }

    private HeroShortcutGroupDTO.Response toResponse(HeroShortcutGroup g) {
        List<HeroShortcutDTO.Response> shortcuts = g.getShortcuts().stream()
                .map(this::toShortcutResponse)
                .toList();
        return new HeroShortcutGroupDTO.Response(
                g.getId(), g.getName(), g.getSortOrder(), shortcuts,
                g.getCreatedAt(), g.getUpdatedAt()
        );
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Override
    public List<HeroShortcutGroupDTO.Response> getAll() {
        try {
            return groupRepository.findAllByOrderBySortOrderAsc().stream()
                    .map(this::toResponse)
                    .toList();
        } catch (Exception e) {
            log.error("Failed to fetch hero shortcut groups", e);
            throw new IntranetException("Failed to fetch hero shortcut groups", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public HeroShortcutGroupDTO.Response create(HeroShortcutGroupDTO.Request req) {
        try {
            HeroShortcutGroup group = new HeroShortcutGroup();
            group.setName(req.getName().trim());
            group.setSortOrder(req.getSortOrder());
            return toResponse(groupRepository.save(group));
        } catch (Exception e) {
            log.error("Failed to create hero shortcut group", e);
            throw new IntranetException("Failed to create group", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public HeroShortcutGroupDTO.Response update(Long id, HeroShortcutGroupDTO.Request req) {
        try {
            HeroShortcutGroup group = groupRepository.findById(id)
                    .orElseThrow(() -> new IntranetException(
                            "Group not found with id: " + id, HttpStatus.NOT_FOUND));

            group.setName(req.getName().trim());
            group.setSortOrder(req.getSortOrder());

            return toResponse(groupRepository.save(group));
        } catch (IntranetException e) {
            log.warn("Business error updating hero shortcut group id: {}", id, e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to update hero shortcut group id: {}", id, e);
            throw new IntranetException("Failed to update group", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        try {
            HeroShortcutGroup group = groupRepository.findById(id)
                    .orElseThrow(() -> new IntranetException(
                            "Group not found with id: " + id, HttpStatus.NOT_FOUND));
            groupRepository.delete(group); // cascades to its shortcuts
        } catch (IntranetException e) {
            log.warn("Group not found with id: {} to delete", id);
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete hero shortcut group id: {}", id, e);
            throw new IntranetException("Failed to delete group", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
