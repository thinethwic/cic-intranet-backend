package com.intranet.cic.services.impl;

import com.intranet.cic.dtos.NavItemDTO;
import com.intranet.cic.entities.NavItem;
import com.intranet.cic.execeptions.IntranetException;
import com.intranet.cic.repositories.NavItemRepository;
import com.intranet.cic.services.NavItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NavItemServiceImpl implements NavItemService {

    private final NavItemRepository navItemRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<NavItem> getTree() {
        try {
            return navItemRepository.findByParentIsNullOrderBySortOrderAsc();
        } catch (Exception exception) {
            log.error("Failed to get nav item tree", exception);
            throw new IntranetException("Failed to get nav items", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public NavItem createNavItem(NavItemDTO navItemDTO) {
        try {
            NavItem item = modelMapper.map(navItemDTO, NavItem.class);
            item.setId(null);

            if (navItemDTO.getParentId() != null) {
                NavItem parent = navItemRepository.findById(navItemDTO.getParentId())
                        .orElseThrow(() -> new IntranetException("Parent nav item not found", HttpStatus.NOT_FOUND));
                item.setParent(parent);
                item.setSortOrder(parent.getChildren().size());
            } else {
                item.setParent(null);
                item.setSortOrder(navItemRepository.findByParentIsNullOrderBySortOrderAsc().size());
            }

            return navItemRepository.save(item);
        } catch (IntranetException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("Failed to create nav item", exception);
            throw new IntranetException("Failed to create nav item", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public NavItem updateNavItem(Long id, NavItemDTO navItemDTO) {
        try {
            NavItem item = navItemRepository.findById(id)
                    .orElseThrow(() -> new IntranetException("Nav item not found", HttpStatus.NOT_FOUND));

            modelMapper.map(navItemDTO, item);
            item.setId(id);

            Long currentParentId = item.getParent() != null ? item.getParent().getId() : null;
            Long requestedParentId = navItemDTO.getParentId();

            if (requestedParentId != null && !requestedParentId.equals(currentParentId)) {
                if (requestedParentId.equals(id)) {
                    throw new IntranetException("A nav item cannot be its own parent", HttpStatus.BAD_REQUEST);
                }
                NavItem newParent = navItemRepository.findById(requestedParentId)
                        .orElseThrow(() -> new IntranetException("Parent nav item not found", HttpStatus.NOT_FOUND));
                assertNoCycle(item, newParent);
                item.setParent(newParent);
            } else if (requestedParentId == null) {
                item.setParent(null);
            }

            return navItemRepository.save(item);
        } catch (IntranetException exception) {
            log.warn("Business error updating nav item id: {}", id, exception);
            throw exception;
        } catch (Exception exception) {
            log.error("Error updating nav item", exception);
            throw new IntranetException("Failed to update nav item", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteNavItem(Long id) {
        try {
            NavItem item = navItemRepository.findById(id)
                    .orElseThrow(() -> new IntranetException("Nav item not found", HttpStatus.NOT_FOUND));
            navItemRepository.delete(item); // cascades to all descendants
        } catch (IntranetException exception) {
            log.warn("Nav item not found with id: {} to delete", id, exception);
            throw exception;
        } catch (Exception exception) {
            log.error("Error deleting nav item", exception);
            throw new IntranetException("Failed to delete nav item", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Walks up from `candidateNewParent` toward the root; if `item` appears in
    // that chain, moving `item` under `candidateNewParent` would create a cycle.
    private void assertNoCycle(NavItem item, NavItem candidateNewParent) {
        NavItem ancestor = candidateNewParent;
        while (ancestor != null) {
            if (ancestor.getId().equals(item.getId())) {
                throw new IntranetException(
                        "Cannot move a nav item under its own descendant", HttpStatus.BAD_REQUEST);
            }
            ancestor = ancestor.getParent();
        }
    }
}
