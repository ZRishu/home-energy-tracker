package org.zr.alert_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.zr.kafka.event.AlertingEvent;

@Slf4j
@Service
public class AlertService {

    @KafkaListener(topics = "energy-alerts", groupId = "alert-service")
    public void energyUsageAlertEvent(AlertingEvent alertingEvent) {
        log.info("Recieved alerting event: {}", alertingEvent);
        
    }
}
