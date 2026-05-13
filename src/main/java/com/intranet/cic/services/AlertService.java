package com.intranet.cic.services;

import com.intranet.cic.dtos.AlertDTO;
import com.intranet.cic.entities.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AlertService {
    Page<Alert> getAllAlerts(Pageable pageable);
    Alert getAlertById (Long id);
    Alert createAlert(AlertDTO alertDTO);
    Alert updateAlert(Long id, AlertDTO alertUpdateDTO);
    void deleteAlert(Long id);

    Page<Alert> getAllAlertsAdmin(Pageable pageable);
}
