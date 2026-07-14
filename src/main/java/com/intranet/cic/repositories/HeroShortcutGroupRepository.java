package com.intranet.cic.repositories;

import com.intranet.cic.entities.HeroShortcutGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HeroShortcutGroupRepository extends JpaRepository<HeroShortcutGroup, Long> {
    List<HeroShortcutGroup> findAllByOrderBySortOrderAsc();
}
