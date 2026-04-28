package com.intranet.cic.controllers.v1;

import com.intranet.cic.controllers.AbstractController;
import com.intranet.cic.dtos.NewsDTO;
import com.intranet.cic.entities.News;
import com.intranet.cic.services.FileStorageService;
import com.intranet.cic.services.NewsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "/api/v1/news")
@RequiredArgsConstructor
@Slf4j
public class NewsController  extends AbstractController {
    private final NewsService newsService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<Page<News>> getAllNews(
            @PageableDefault(size = 10, sort = "id") Pageable pageable
    ) {
        return sendOkResponse(newsService.getAllNews(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<News> getNewsById(@PathVariable Long id) {
        return sendOkResponse(newsService.getNewsById(id));
    }

    // ✅ multipart — image is optional
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<News> createNews(
            @RequestPart("data") @Valid NewsDTO newsDTO,
            @RequestPart(value = "image", required = false) MultipartFile image  // ✅ optional
    ) {
        if (image != null && !image.isEmpty()) {
            String imageUrl = fileStorageService.storeImage(image);
            newsDTO.setImage(imageUrl);
        }
        return sendCreatedResponse(newsService.createNews(newsDTO));
    }

    // ✅ New endpoint: image update only
    @PutMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<News> updateNewsImage(
            @PathVariable Long id,
            @RequestPart("image") MultipartFile image
    ) {
        News existing = newsService.getNewsById(id);
        if (existing.getImage() != null) {
            fileStorageService.deleteFile(existing.getImage());
        }
        String imageUrl = fileStorageService.storeImage(image);
        return sendOkResponse(newsService.updateNewsImage(id, imageUrl));
    }

    // ✅ Change PUT /{id} to accept JSON only
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<News> updateNews(
            @PathVariable Long id,
            @RequestBody @Valid NewsDTO newsDTO
    ) {
        return sendOkResponse(newsService.updateNews(id, newsDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNews(@PathVariable Long id) {
        // ✅ Delete physical image file when news is deleted
        News news = newsService.getNewsById(id);
        if (news.getImage() != null) {
            fileStorageService.deleteFile(news.getImage());
        }
        newsService.deleteNews(id);
        return sendNoContentResponse();
    }
}
