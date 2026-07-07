package com.intranet.cic.repositories;

import com.intranet.cic.entities.Task;
import com.intranet.cic.entities.TaskAttachment;
import com.intranet.cic.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {

    List<TaskAttachment> findByTaskOrderByUploadedAtAsc(Task task);

    Optional<TaskAttachment> findByIdAndTask(Long id, Task task);

    // CAST(:q AS string) — see TaskRepository.findMyTasks for why this is needed.
    @Query("SELECT a FROM TaskAttachment a WHERE a.task.owner = :owner " +
            "AND (:q IS NULL OR LOWER(a.fileName) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%')))")
    Page<TaskAttachment> findMyDocuments(@Param("owner") User owner,
                                          @Param("q") String q,
                                          Pageable pageable);
}