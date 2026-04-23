package com.intranet.cic.controllers.v1;

import com.intranet.cic.controllers.AbstractController;
import com.intranet.cic.dtos.NewsDTO;
import com.intranet.cic.entities.News;
import com.intranet.cic.services.NewsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/news")
@RequiredArgsConstructor
@Slf4j
public class NewsController  extends AbstractController {
    private final NewsService newsService;

    @GetMapping
    public ResponseEntity<Page<News>> getAllNews(
            @PageableDefault(size = 10, sort = "id") Pageable pageable
    ) {
        Page<News> news = newsService.getAllNews(pageable);
        return sendOkResponse(news);
    }

    @GetMapping("/{id}")
    public ResponseEntity<News> getNewsById(@PathVariable Long id) {
        return sendOkResponse(newsService.getNewsById(id));
    }

    @PostMapping
    public ResponseEntity<News> createNews(
            @Valid @RequestBody NewsDTO newsDTO
    ) {
        News news = newsService.createNews(newsDTO);
        return sendCreatedResponse(news);
    }

    @PutMapping("/{id}")
    public ResponseEntity<News> updateNews(
            @PathVariable Long id,
            @Valid @RequestBody NewsDTO newsDTO
    ) {
        News news = newsService.updateNews(id, newsDTO);
        return sendOkResponse(news);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNews(@PathVariable Long id) {
        newsService.deleteNews(id);
        return sendNoContentResponse();
    }
}
