package com.intranet.cic.schedulers;

import com.intranet.cic.repositories.NewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class HotNewsDemotionScheduler {

    private final NewsRepository newsRepository;

    // Runs every day at midnight
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void demoteExpiredHotNews() {
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(1);
        int updated = newsRepository.demoteHotNewsOlderThan(cutoff);

        if (updated > 0) {
            log.info("Hot news demotion — {} article(s) demoted to normal.", updated);
        } else {
            log.debug("Hot news demotion — no articles to demote.");
        }
    }
}
