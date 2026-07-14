package com.intranet.cic.services;

import com.intranet.cic.dtos.HeroShortcutGroupDTO;

import java.util.List;

public interface HeroShortcutGroupService {
    List<HeroShortcutGroupDTO.Response> getAll();
    HeroShortcutGroupDTO.Response create(HeroShortcutGroupDTO.Request request);
    HeroShortcutGroupDTO.Response update(Long id, HeroShortcutGroupDTO.Request request);
    void delete(Long id);
}
