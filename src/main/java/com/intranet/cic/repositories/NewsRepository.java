package com.intranet.cic.repositories;

import com.intranet.cic.entities.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface NewsRepository extends JpaRepository<News, Long> {
    @Modifying
    @Query("UPDATE News n SET n.isHot = false WHERE n.isHot = true AND n.createdAt < :cutoff")
    int demoteHotNewsOlderThan(@Param("cutoff") LocalDateTime cutoff);

    Page<News> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
