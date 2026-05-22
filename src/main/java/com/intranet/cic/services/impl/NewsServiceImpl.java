package com.intranet.cic.services.impl;

import com.intranet.cic.dtos.NewsDTO;
import com.intranet.cic.entities.News;
import com.intranet.cic.entities.User;
import com.intranet.cic.execeptions.IntranetException;
import com.intranet.cic.repositories.NewsRepository;
import com.intranet.cic.repositories.UserRepository;
import com.intranet.cic.services.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsServiceImpl implements NewsService {

    private final NewsRepository newsRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public Page<News> getAllNews(Pageable pageable) {
        try {
            Pageable sorted = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt")  // ← newest first
            );
            return newsRepository.findAll(sorted);
        } catch (Exception exception) {
            log.error("Failed to get all news", exception);
            throw new IntranetException("Failed to get all news", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public News getNewsById(Long id) {
        try{
            return newsRepository.findById(id)
                    .orElseThrow(()-> new IntranetException("News Not found", HttpStatus.NOT_FOUND)
                    );
        } catch (IntranetException intranetException) {

            log.warn("News not found with id: {} to fetch", id, intranetException);
            throw new IntranetException("News Not found", HttpStatus.NOT_FOUND);
        } catch (Exception exception) {
            log.error("Error getting news", exception);
            throw new IntranetException("Failed to get news", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public News createNews(NewsDTO newsDTO) {
        try {
            // Check for duplicate title
            if (newsRepository.existsByTitle(newsDTO.getTitle())) {
                throw new IntranetException("A news article with this title already exists", HttpStatus.CONFLICT);
            }

            User author = userRepository.findById(newsDTO.getAuthorId())
                    .orElseThrow(() -> new IntranetException("Author not found", HttpStatus.NOT_FOUND));

            News news = modelMapper.map(newsDTO, News.class);
            news.setAuthor(author);

            if (Boolean.TRUE.equals(newsDTO.getIsHot())) {
                news.setIsHot(true);
                news.setHotSince(LocalDateTime.now());
            } else {
                news.setIsHot(false);
                news.setHotSince(null);
            }

            return newsRepository.save(news);

        } catch (IntranetException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create news", e);
            throw new IntranetException("Failed to create news", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public News updateNews(Long id, NewsDTO newsDTO) {
        try {
            News news = newsRepository.findById(id)
                    .orElseThrow(() -> new IntranetException("News not found", HttpStatus.NOT_FOUND));

            String existingImage = news.getImage();         // save before ModelMapper wipes it
            boolean wasHot       = news.getIsHot();            // save before ModelMapper overwrites it
            LocalDateTime existingHotSince = news.getHotSince();

            modelMapper.map(newsDTO, news);

            // Restore image if no new image was provided
            if (newsDTO.getImage() == null) {
                news.setImage(existingImage);
            }

            if (!news.getTitle().equals(newsDTO.getTitle())
                    && newsRepository.existsByTitle(newsDTO.getTitle())) {
                throw new IntranetException("A news article with this title already exists", HttpStatus.CONFLICT);
            }

            // Handle hot/hotSince transition correctly
            boolean isNowHot = Boolean.TRUE.equals(newsDTO.getIsHot());

            if (isNowHot && !wasHot) {
                // Article is being promoted to hot — stamp the time
                news.setIsHot(true);
                news.setHotSince(LocalDateTime.now());

            } else if (!isNowHot && wasHot) {
                // Article is being manually demoted — clear the stamp
                news.setIsHot(false);
                news.setHotSince(null);

            } else {
                // Hot status unchanged — preserve the original hotSince so the
                // scheduler expiry clock is not reset on unrelated edits
                news.setHotSince(existingHotSince);
            }

            if (newsDTO.getAuthorId() != null) {
                User author = userRepository.findById(newsDTO.getAuthorId())
                        .orElseThrow(() -> new IntranetException("Author not found", HttpStatus.NOT_FOUND));
                news.setAuthor(author);
            }

            return newsRepository.save(news);

        } catch (IntranetException e) {
            log.warn("Business error updating news id: {}", id, e);
            throw e;
        } catch (Exception e) {
            log.error("Error updating news id: {}", id, e);
            throw new IntranetException("Failed to update news", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public News updateNewsImage(Long id, String imageUrl) {
        try {
            News news = newsRepository.findById(id)
                    .orElseThrow(() -> new IntranetException("News not found", HttpStatus.NOT_FOUND));
            news.setImage(imageUrl);
            return newsRepository.save(news);

        } catch (IntranetException e) {
            log.warn("News not found with id: {} to update image", id, e);
            throw e;
        } catch (Exception e) {
            log.error("Error updating image for news id: {}", id, e);
            throw new IntranetException("Failed to update news image", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteNews(Long id) {
        try{
            News news = newsRepository.findById(id)
                    .orElseThrow(()-> new IntranetException("News Not found", HttpStatus.NOT_FOUND)
                    );
            newsRepository.delete(news);
        }  catch (IntranetException intranetException) {

            log.warn("News not found with id: {} to fetch", id, intranetException);
            throw new IntranetException("News Not found", HttpStatus.NOT_FOUND);

        } catch (Exception exception) {

            log.error("Error updating News", exception);
            throw new IntranetException("Failed to update News", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
