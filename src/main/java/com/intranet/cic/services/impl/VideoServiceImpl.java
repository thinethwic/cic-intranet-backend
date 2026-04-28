package com.intranet.cic.services.impl;

import com.intranet.cic.dtos.VideoDTO;
import com.intranet.cic.entities.User;
import com.intranet.cic.entities.Video;
import com.intranet.cic.execeptions.IntranetException;
import com.intranet.cic.repositories.UserRepository;
import com.intranet.cic.repositories.VideoRepository;
import com.intranet.cic.services.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoServiceImpl implements VideoService {

    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public Page<Video> getAllVideos(Pageable pageable) {
        try {
            return videoRepository.findAll(pageable);
        } catch (Exception exception) {
            log.error("Failed to get all videos", exception);
            throw new IntranetException("Failed to get all videos", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Video getVideoById(Long id) {
        try{
            return videoRepository.findById(id)
                    .orElseThrow(() -> new IntranetException("Video Not found", HttpStatus.NOT_FOUND)
                    );
        } catch (IntranetException intranetException) {

            log.warn("Video not found with id: {} to fetch", id, intranetException);
            throw new IntranetException("Mentor Not found", HttpStatus.NOT_FOUND);
        } catch (Exception exception) {
            log.error("Error getting video", exception);
            throw new IntranetException("Failed to get video", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Video createVideo(VideoDTO videoDTO) {
        try{
            User user = userRepository.findById(videoDTO.getUserId())
                    .orElseThrow(() -> new IntranetException("User not found", HttpStatus.NOT_FOUND));

            Video video = modelMapper.map(videoDTO, Video.class);
            video.setUser(user);

            return videoRepository.save(video);
        } catch (Exception exception){
            log.error("Failed to create video", exception);
            throw new IntranetException("Failed to create video", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Video updateVideo(Long id, VideoDTO videoDTO) {
        try {
            Video video = videoRepository.findById(id)
                    .orElseThrow(() -> new IntranetException("Video not found", HttpStatus.NOT_FOUND));

            modelMapper.map(videoDTO, video); // ✅ map into existing entity

            if (videoDTO.getUserId() != null) {
                User user = userRepository.findById(videoDTO.getUserId())
                        .orElseThrow(() -> new IntranetException("User not found", HttpStatus.NOT_FOUND));
                video.setUser(user); // ✅ override after modelMapper
            }

            return videoRepository.save(video);

        } catch (IntranetException e) {
            log.warn("Business error updating video id: {}", id, e);
            throw e;
        } catch (Exception e) {
            log.error("Error updating video id: {}", id, e);
            throw new IntranetException("Failed to update video", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteVideo(Long id) {
        try{

            Video video = videoRepository.findById(id)
                    .orElseThrow(() -> new IntranetException("Video Not found", HttpStatus.NOT_FOUND)
                    );

            videoRepository.delete(video);

        } catch (IntranetException intranetException) {

            log.warn("Video not found with id: {} to fetch", id, intranetException);
            throw new IntranetException("Video Not found", HttpStatus.NOT_FOUND);

        } catch (Exception exception) {

            log.error("Error deleting video", exception);
            throw new IntranetException("Failed to delete video", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
