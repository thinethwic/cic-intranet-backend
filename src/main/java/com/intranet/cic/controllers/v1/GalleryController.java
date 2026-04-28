package com.intranet.cic.controllers.v1;

import com.intranet.cic.controllers.AbstractController;
import com.intranet.cic.dtos.GalleryDTO;
import com.intranet.cic.entities.Gallery;
import com.intranet.cic.services.FileStorageService;
import com.intranet.cic.services.GalleryService;
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
@RequestMapping(path = "/api/v1/images")
@RequiredArgsConstructor
@Slf4j
public class GalleryController extends AbstractController {

    private final GalleryService galleryService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<Page<Gallery>> getAllImages(
            @PageableDefault(size = 10, sort = "id") Pageable pageable
    ) {
        return sendOkResponse(galleryService.getAllImages(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Gallery> getImageById(@PathVariable Long id) {
        return sendOkResponse(galleryService.getImageById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Gallery> createImage(
            @RequestPart("data") @Valid GalleryDTO galleryDTO,
            @RequestPart("image") MultipartFile image
    ) {
        String imageUrl = fileStorageService.storeImage(image);
        galleryDTO.setImage(imageUrl);
        return sendCreatedResponse(galleryService.createImage(galleryDTO));
    }

    // ✅ multipart — optional image replacement on update
    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Gallery> updateImage(
            @PathVariable Long id,
            @RequestPart("data") @Valid GalleryDTO galleryDTO,
            @RequestPart(value = "image", required = false) MultipartFile image  // ✅ optional
    ) {
        if (image != null && !image.isEmpty()) {
            // ✅ Delete old image before storing new one
            Gallery existing = galleryService.getImageById(id);
            fileStorageService.deleteFile(existing.getImage());

            String imageUrl = fileStorageService.storeImage(image);
            galleryDTO.setImage(imageUrl);
        }
        return sendOkResponse(galleryService.updateImage(id, galleryDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        Gallery gallery = galleryService.getImageById(id);
        fileStorageService.deleteFile(gallery.getImage());
        galleryService.deleteImage(id);
        return sendNoContentResponse();
    }
}
