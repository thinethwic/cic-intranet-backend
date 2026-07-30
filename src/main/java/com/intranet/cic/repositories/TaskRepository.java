package com.intranet.cic.repositories;

import com.intranet.cic.entities.Task;
import com.intranet.cic.entities.User;
import com.intranet.cic.entities.types.TaskCategory;
import com.intranet.cic.entities.types.TaskPriority;
import com.intranet.cic.entities.types.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // CAST(:q AS string) avoids Postgres binding a null :q as bytea inside
    // LOWER(CONCAT(...)) — without it, Hibernate can't infer the parameter's
    // type from a null value alone and the query fails with
    // "function lower(bytea) does not exist". The same applies to the
    // ":dueDateFrom"/":dueDateTo" LocalDate params — every occurrence needs
    // an explicit CAST to date (including the bare "IS NULL" check), or
    // Postgres either can't determine a type for the parameter at all
    // ("could not determine data type of parameter") or Hibernate defaults
    // its type to bytea, which then can't be cast to date.
    @Query("SELECT t FROM Task t WHERE t.owner = :owner " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:priority IS NULL OR t.priority = :priority) " +
            "AND (:category IS NULL OR t.category = :category) " +
            "AND (CAST(:dueDateFrom AS date) IS NULL OR t.dueDate >= CAST(:dueDateFrom AS date)) " +
            "AND (CAST(:dueDateTo AS date) IS NULL OR t.dueDate <= CAST(:dueDateTo AS date)) " +
            "AND (:q IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%')))")
    Page<Task> findMyTasks(@Param("owner") User owner,
                            @Param("status") TaskStatus status,
                            @Param("priority") TaskPriority priority,
                            @Param("category") TaskCategory category,
                            @Param("dueDateFrom") LocalDate dueDateFrom,
                            @Param("dueDateTo") LocalDate dueDateTo,
                            @Param("q") String q,
                            Pageable pageable);
}