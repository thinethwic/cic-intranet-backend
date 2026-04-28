package com.intranet.cic.services;

import com.intranet.cic.dtos.VideoDTO;
import com.intranet.cic.entities.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VideoService {

    Page<Video> getAllVideos(Pageable pageable);
    Video getVideoById(Long id);
    Video createVideo(VideoDTO videoDTO);
    Video updateVideo(Long id, VideoDTO videoDTO);
    void deleteVideo(Long id);
}
