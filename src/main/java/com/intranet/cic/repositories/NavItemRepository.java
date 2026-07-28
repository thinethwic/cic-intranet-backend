package com.intranet.cic.repositories;

import com.intranet.cic.entities.NavItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NavItemRepository extends JpaRepository<NavItem, Long> {
    List<NavItem> findByParentIsNullOrderBySortOrderAsc();
    List<NavItem> findByParentIdOrderBySortOrderAsc(Long parentId);
}
