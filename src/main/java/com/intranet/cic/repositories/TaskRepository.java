package com.intranet.cic.repositories;

import com.intranet.cic.entities.Task;
import com.intranet.cic.entities.User;
import com.intranet.cic.entities.types.TaskPriority;
import com.intranet.cic.entities.types.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // CAST(:q AS string) avoids Postgres binding a null :q as bytea inside
    // LOWER(CONCAT(...)) — without it, Hibernate can't infer the parameter's
    // type from a null value alone and the query fails with
    // "function lower(bytea) does not exist".
    @Query("SELECT t FROM Task t WHERE t.owner = :owner " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:priority IS NULL OR t.priority = :priority) " +
            "AND (:q IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%')))")
    Page<Task> findMyTasks(@Param("owner") User owner,
                            @Param("status") TaskStatus status,
                            @Param("priority") TaskPriority priority,
                            @Param("q") String q,
                            Pageable pageable);
}