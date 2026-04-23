package com.intranet.cic.services.impl;

import com.intranet.cic.dtos.GalleryDTO;
import com.intranet.cic.entities.Gallery;
import com.intranet.cic.entities.User;
import com.intranet.cic.execeptions.IntranetException;
import com.intranet.cic.repositories.GalleryRepository;
import com.intranet.cic.repositories.UserRepository;
import com.intranet.cic.services.GalleryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GalleryServiceImpl implements GalleryService {

    private final GalleryRepository galleryRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    @Override
    public Page<Gallery> getAllImages(Pageable pageable) {
        try{
            return galleryRepository.findAll(pageable);
        } catch (Exception exception){
            log.error("Failed to get all images", exception);
            throw new IntranetException("Failed to get all images", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Gallery getImageById(Long id) {
        try{
            return galleryRepository.findById(id)
                    .orElseThrow(()-> new IntranetException("Image Not found", HttpStatus.NOT_FOUND)
                    );
        } catch (IntranetException intranetException) {

            log.warn("Image not found with id: {} to fetch", id, intranetException);
            throw new IntranetException("Image Not found", HttpStatus.NOT_FOUND);
        } catch (Exception exception) {
            log.error("Error getting image", exception);
            throw new IntranetException("Failed to get image", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Gallery createImage(GalleryDTO galleryDTO) {
        try{
            User user = userRepository.findById(galleryDTO.getUserId())
                    .orElseThrow(() -> new IntranetException("User Not found", HttpStatus.NOT_FOUND));

            Gallery gallery = modelMapper.map(galleryDTO, Gallery.class);
            gallery.setUser(user);

            return galleryRepository.save(gallery);
        } catch (Exception exception){
            log.error("Failed to create image", exception);
            throw new IntranetException("Failed to create image", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Gallery updateImage(Long id, GalleryDTO galleryDTO) {
        try{
            Gallery gallery  =  galleryRepository.findById(id)
                    .orElseThrow(()-> new IntranetException("Image Not found", HttpStatus.NOT_FOUND)
                    );

            User user = userRepository.findById(galleryDTO.getUserId())
                    .orElseThrow(() -> new IntranetException("User Not found", HttpStatus.NOT_FOUND));

            modelMapper.map(galleryDTO, Gallery.class);
            gallery.setUser(user);

            return galleryRepository.save(gallery);
        }  catch (IntranetException intranetException) {

            log.warn("Image not found with id: {} to fetch", id, intranetException);
            throw new IntranetException("Image Not found", HttpStatus.NOT_FOUND);

        } catch (Exception exception) {

            log.error("Error updating Image", exception);
            throw new IntranetException("Failed to update Image", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteImage(Long id) {
        try{
            Gallery gallery  =  galleryRepository.findById(id)
                    .orElseThrow(()-> new IntranetException("Image Not found", HttpStatus.NOT_FOUND)
                    );

            galleryRepository.delete(gallery);
        }  catch (IntranetException intranetException) {

            log.warn("Image not found with id: {} to fetch", id, intranetException);
            throw new IntranetException("Image Not found", HttpStatus.NOT_FOUND);

        } catch (Exception exception) {
            log.error("Error updating Image", exception);
            throw new IntranetException("Failed to update Image", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
