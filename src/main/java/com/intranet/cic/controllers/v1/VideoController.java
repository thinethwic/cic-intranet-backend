package com.intranet.cic.controllers.v1;

import com.intranet.cic.controllers.AbstractController;
import com.intranet.cic.dtos.VideoDTO;
import com.intranet.cic.entities.Video;
import com.intranet.cic.services.VideoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/videos")
@RequiredArgsConstructor
@Slf4j
public class VideoController extends AbstractController {
    private final VideoService videoService;
    private final ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<Page<Video>> getAllVideos(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return sendOkResponse(videoService.getAllVideos(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Video> getVideoById(@PathVariable Long id) {
        log.info("Request to get video with id: {}", id);
        Video video = videoService.getVideoById(id);
        return sendOkResponse(video);
    }

    @PostMapping
    public ResponseEntity<Video> createVideo(@Valid @RequestBody VideoDTO videoDTO) {
        log.info("Request to create video with title: {}", videoDTO.getTitle());
        Video created = videoService.createVideo(videoDTO);
        return sendCreatedResponse(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Video> updateVideo(
            @PathVariable Long id,
            @Valid @RequestBody VideoDTO videoDTO
    ) {
        log.info("Request to update video with id: {}", id);
        Video updated = videoService.updateVideo(id, videoDTO);
        return sendOkResponse(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long id) {
        log.info("Request to delete video with id: {}", id);
        videoService.deleteVideo(id);
        return sendNoContentResponse();
    }

}
