package com.intranet.cic.services;
import com.intranet.cic.dtos.NewsDTO;
import com.intranet.cic.entities.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NewsService {
    Page<News> getAllNews(Pageable pageable);
    News getNewsById(Long id);
    News createNews(NewsDTO newsDTO);
    News updateNews(Long id, NewsDTO newsDTO);
    void deleteNews(Long id);
}
