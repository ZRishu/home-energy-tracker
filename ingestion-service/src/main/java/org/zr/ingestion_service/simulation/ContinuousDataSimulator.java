package org.zr.ingestion_service.simulation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.zr.ingestion_service.dto.EnergyUsageDto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;

@Slf4j
@Component
public class ContinuousDataSimulator implements CommandLineRunner {
    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();

    @Value("${simulation.request-per-interval}")
    private int requestPerInterval;

    @Value("${simulation.endpoint}")
    private String ingestionEndpoint;

    @Override
    public void run(String... args) throws Exception {
        log.info("Continuous Data Simulator started");
    }

    // @Scheduled(fixedRateString = "${simulation.interval-ms}")
    private void sendMockData() {
        for (int i = 0; i < requestPerInterval; i++) {
            EnergyUsageDto energyUsageDto = EnergyUsageDto.builder()
                    .deviceId(random.nextLong(1, 6))
                    .energyConsumed(Math.round(random.nextDouble(0.0, 2.0) * 100.0) / 100.0)
                    .timestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())
                    .build();

            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<EnergyUsageDto> entity = new HttpEntity<>(energyUsageDto, headers);
                restTemplate.postForEntity(ingestionEndpoint, entity, Void.class);

                log.info("Sent mock data: {}", energyUsageDto);
            } catch (Exception e) {
                log.error("Failed to send mock data: {}", e.getMessage());
            }
        }
    }
}
