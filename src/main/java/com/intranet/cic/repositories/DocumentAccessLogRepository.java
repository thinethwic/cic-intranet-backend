package com.intranet.cic.repositories;

import com.intranet.cic.entities.DocumentAccessLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentAccessLogRepository extends JpaRepository<DocumentAccessLog, Long> {
    Page<DocumentAccessLog> findByDocumentId(Long documentId, Pageable pageable);
    Page<DocumentAccessLog> findByUserId(Long userId, Pageable pageable);
}
