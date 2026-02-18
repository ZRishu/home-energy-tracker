package org.zr.alert_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.zr.kafka.event.AlertingEvent;

@Slf4j
@Service
public class AlertService {

    private final EmailService emailService;

    public AlertService(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "energy-alerts", groupId = "alert-service")
    public void energyUsageAlertEvent(AlertingEvent alertingEvent) {
        log.info("Recieved alerting event: {}", alertingEvent);

        // Send email alert
        final String subject = "Energy usage alert for User: " + alertingEvent.getUserId();
        final String message = "Alert: " + alertingEvent.getMessage() +
                "\nThreshold: " + alertingEvent.getThreshold() +
                "\nEnergy consumed: " + alertingEvent.getEnergyConsumed();

        emailService.sendEmail(alertingEvent.getEmail(), subject, message, alertingEvent.getUserId());
    }
}
