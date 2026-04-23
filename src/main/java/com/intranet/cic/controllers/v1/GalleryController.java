package com.intranet.cic.controllers.v1;

import com.intranet.cic.controllers.AbstractController;
import com.intranet.cic.dtos.GalleryDTO;
import com.intranet.cic.entities.Gallery;
import com.intranet.cic.services.GalleryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/images")
@RequiredArgsConstructor
@Slf4j
public class GalleryController extends AbstractController {

    private final GalleryService galleryService;

    @GetMapping
    public ResponseEntity<Page<Gallery>> getAllImages(
            @PageableDefault(size = 10, sort = "id") Pageable pageable
    ) {
        Page<Gallery> images = galleryService.getAllImages(pageable);
        return sendOkResponse(images);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Gallery> getImageById(@PathVariable Long id) {
        return sendOkResponse(galleryService.getImageById(id));
    }

    @PostMapping
    public ResponseEntity<Gallery> createImage(
            @Valid @RequestBody GalleryDTO galleryDTO
    ) {
        Gallery gallery = galleryService.createImage(galleryDTO);
        return sendCreatedResponse(gallery);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Gallery> updateImage(
            @PathVariable Long id,
            @Valid @RequestBody GalleryDTO galleryDTO
    ) {
        Gallery gallery = galleryService.updateImage(id, galleryDTO);
        return sendOkResponse(gallery);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        galleryService.deleteImage(id);
        return sendNoContentResponse();
    }
}
