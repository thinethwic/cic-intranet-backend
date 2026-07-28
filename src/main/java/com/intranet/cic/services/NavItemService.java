package com.intranet.cic.services;

import com.intranet.cic.dtos.NavItemDTO;
import com.intranet.cic.entities.NavItem;

import java.util.List;

public interface NavItemService {
    List<NavItem> getTree();
    NavItem createNavItem(NavItemDTO navItemDTO);
    NavItem updateNavItem(Long id, NavItemDTO navItemDTO);
    void deleteNavItem(Long id);
}
