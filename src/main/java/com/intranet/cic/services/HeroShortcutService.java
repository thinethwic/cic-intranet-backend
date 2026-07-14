package com.intranet.cic.services;

import com.intranet.cic.dtos.HeroShortcutDTO;

public interface HeroShortcutService {
    HeroShortcutDTO.Response create(HeroShortcutDTO.Request request);
    HeroShortcutDTO.Response update(Long id, HeroShortcutDTO.Request request);
    void delete(Long id);
}
