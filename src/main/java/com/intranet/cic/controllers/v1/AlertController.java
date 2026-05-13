package com.intranet.cic.controllers.v1;

import com.intranet.cic.controllers.AbstractController;
import com.intranet.cic.dtos.AlertDTO;
import com.intranet.cic.entities.Alert;
import com.intranet.cic.services.AlertService;
import com.intranet.cic.services.FileStorageService;
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
@RequestMapping(path = "/api/v1/alerts")
@RequiredArgsConstructor
@Slf4j
public class AlertController extends AbstractController {

    private final AlertService alertService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<Page<Alert>> getAllAlerts(
            @PageableDefault(size = 10, sort = "id") Pageable pageable
    ) {
        return sendOkResponse(alertService.getAllAlerts(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Alert> getAlertById(@PathVariable Long id) {
        return sendOkResponse(alertService.getAlertById(id));
    }

    // AlertController.java — add this endpoint
    @GetMapping("/all")
    public ResponseEntity<Page<Alert>> getAllAlertsAdmin(
            @PageableDefault(size = 100, sort = "id") Pageable pageable
    ) {
        return sendOkResponse(alertService.getAllAlertsAdmin(pageable));
    }

    // Flyer is optional — some alerts are text-only
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Alert> createAlert(
            @RequestPart("data") @Valid AlertDTO alertDTO,
            @RequestPart(value = "flyer", required = false) MultipartFile flyer
    ) {
        if (flyer != null && !flyer.isEmpty()) {
            String flyerUrl = fileStorageService.storeImage(flyer);
            alertDTO.setFlyerImage(flyerUrl);
        }
        return sendCreatedResponse(alertService.createAlert(alertDTO));
    }

    // Flyer-only update endpoint
    @PutMapping(value = "/{id}/flyer", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Alert> updateAlertFlyer(
            @PathVariable Long id,
            @RequestPart("flyer") MultipartFile flyer
    ) {
        // Delete old flyer from filesystem before storing the new one
        Alert existing = alertService.getAlertById(id);
        if (existing.getFlyerImage() != null) {
            fileStorageService.deleteFile(existing.getFlyerImage());
        }
        String flyerUrl = fileStorageService.storeImage(flyer);
        AlertDTO alertDTO = new AlertDTO();
        alertDTO.setFlyerImage(flyerUrl);
        return sendOkResponse(alertService.updateAlert(id, alertDTO));
    }

    // JSON-only update — no file involved
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Alert> updateAlert(
            @PathVariable Long id,
            @RequestBody @Valid AlertDTO alertDTO
    ) {
        return sendOkResponse(alertService.updateAlert(id, alertDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long id) {
        // Delete physical flyer file from filesystem when alert is deleted
        Alert alert = alertService.getAlertById(id);
        if (alert.getFlyerImage() != null) {
            fileStorageService.deleteFile(alert.getFlyerImage());
        }
        alertService.deleteAlert(id);
        return sendNoContentResponse();
    }
}
