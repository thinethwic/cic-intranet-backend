package com.intranet.cic.repositories;

import com.intranet.cic.entities.Department;
import com.intranet.cic.entities.types.Segment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findAllByOrderBySegmentAscNameAsc();

    List<Department> findBySegmentOrderByNameAsc(Segment segment);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    boolean existsByNameIgnoreCaseAndSegment(String name, Segment segment);

    boolean existsByNameIgnoreCaseAndSegmentAndIdNot(String name, Segment segment, Long id);
}
