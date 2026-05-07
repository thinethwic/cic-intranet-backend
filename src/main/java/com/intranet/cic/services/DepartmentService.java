package com.intranet.cic.services;

import com.intranet.cic.dtos.DepartmentDTO;
import com.intranet.cic.entities.types.Segment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DepartmentService {
    Page<DepartmentDTO.Response> getAll(int page, int size);

    List<DepartmentDTO.Response> getBySegment(Segment segment);

    DepartmentDTO.Response getById(Long id);

    DepartmentDTO.Response create(DepartmentDTO.Request request);

    DepartmentDTO.Response update(Long id, DepartmentDTO.Request request);

    void delete(Long id);
}
