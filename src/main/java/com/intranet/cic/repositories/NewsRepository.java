package com.intranet.cic.repositories;

import com.intranet.cic.entities.News;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<News, Long> {
}
