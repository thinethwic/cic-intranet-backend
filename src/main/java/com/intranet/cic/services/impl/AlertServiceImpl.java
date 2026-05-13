package com.intranet.cic.services.impl;

import com.intranet.cic.dtos.AlertDTO;
import com.intranet.cic.entities.Alert;
import com.intranet.cic.entities.User;
import com.intranet.cic.execeptions.IntranetException;
import com.intranet.cic.repositories.AlertRepository;
import com.intranet.cic.repositories.UserRepository;
import com.intranet.cic.services.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public Page<Alert> getAllAlerts(Pageable pageable) {
        try {
            Pageable sorted = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt")
            );
            return alertRepository.findAllPublished(LocalDate.now(), sorted);
        } catch (Exception e) {
            log.error("Failed to get all alerts", e);
            throw new IntranetException("Failed to get all alerts", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Alert getAlertById(Long id) {
        try {
            return alertRepository.findById(id)
                    .orElseThrow(() -> new IntranetException("Alert not found", HttpStatus.NOT_FOUND));
        } catch (IntranetException e) {
            log.warn("Alert not found with id: {} to fetch", id, e);
            throw e;
        } catch (Exception e) {
            log.error("Error getting alert with id: {}", id, e);
            throw new IntranetException("Failed to get alert", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Page<Alert> getAllAlertsAdmin(Pageable pageable) {
        try {
            Pageable sorted = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt")
            );
            return alertRepository.findAll(sorted); // ← no date filter
        } catch (Exception e) {
            log.error("Failed to get all alerts for admin", e);
            throw new IntranetException("Failed to get all alerts", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Alert createAlert(AlertDTO alertDTO) {
        try {
            User user = userRepository.findById(alertDTO.getUserId())
                    .orElseThrow(() -> new IntranetException("User not found", HttpStatus.NOT_FOUND));

            Alert alert = modelMapper.map(alertDTO, Alert.class);
            alert.setUser(user);

            return alertRepository.save(alert);

        } catch (IntranetException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create alert", e);
            throw new IntranetException("Failed to create alert", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Alert updateAlert(Long id, AlertDTO alertDTO) {
        try {
            Alert alert = alertRepository.findById(id)
                    .orElseThrow(() -> new IntranetException("Alert not found", HttpStatus.NOT_FOUND));

            // Preserve flyerImage if no new one is provided
            String existingFlyerImage = alert.getFlyerImage();

            modelMapper.map(alertDTO, alert);

            if (alertDTO.getFlyerImage() == null) {
                alert.setFlyerImage(existingFlyerImage);
            }

            if (alertDTO.getUserId() != null) {
                User user = userRepository.findById(alertDTO.getUserId())
                        .orElseThrow(() -> new IntranetException("User not found", HttpStatus.NOT_FOUND));
                alert.setUser(user);
            }

            return alertRepository.save(alert);

        } catch (IntranetException e) {
            log.warn("Business error updating alert id: {}", id, e);
            throw e;
        } catch (Exception e) {
            log.error("Error updating alert id: {}", id, e);
            throw new IntranetException("Failed to update alert", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteAlert(Long id) {
        try {
            Alert alert = alertRepository.findById(id)
                    .orElseThrow(() -> new IntranetException("Alert not found", HttpStatus.NOT_FOUND));
            alertRepository.delete(alert);
        } catch (IntranetException e) {
            log.warn("Alert not found with id: {} to delete", id, e);
            throw e;
        } catch (Exception e) {
            log.error("Error deleting alert id: {}", id, e);
            throw new IntranetException("Failed to delete alert", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}