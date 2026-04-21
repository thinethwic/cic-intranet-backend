package com.intranet.cic.services;
import com.intranet.cic.dtos.NewsDTO;
import com.intranet.cic.entities.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NewsService {
    Page<News> getAllUsers(Pageable pageable);
    News getUserById(Long id);
    News createUser(NewsDTO newsDTO);
    News updateUser(Long id, NewsDTO newsDTO);
    void deleteUser(Long id);
}
