package com.intranet.cic.services;

import com.intranet.cic.dtos.VideoDTO;
import com.intranet.cic.entities.Video;

import java.util.List;

public interface VideoService {

    List<Video> getAllVideos();
    Video getVideoById(Long id);
    Video createVideo(VideoDTO videoDTO);
    Video updateVideo(Long id, VideoDTO videoDTO);
    void deleteVideo(Long id);
}
