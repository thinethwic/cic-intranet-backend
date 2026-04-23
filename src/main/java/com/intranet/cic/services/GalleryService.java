package com.intranet.cic.services;

import com.intranet.cic.dtos.GalleryDTO;
import com.intranet.cic.entities.Gallery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GalleryService {
    Page<Gallery> getAllImages(Pageable pageable);
    Gallery getImageById(Long id);
    Gallery createImage(GalleryDTO galleryDTO);
    Gallery updateImage(Long id, GalleryDTO galleryDTO);
    void deleteImage(Long id);
}
