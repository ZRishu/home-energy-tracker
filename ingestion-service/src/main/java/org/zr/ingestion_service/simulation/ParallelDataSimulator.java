package org.zr.ingestion_service.simulation;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.zr.ingestion_service.dto.EnergyUsageDto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Component
@Slf4j
public class ParallelDataSimulator implements CommandLineRunner {
    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();

    @Value("${simulation.parallel-threads}")
    private int parallelThreads;

    @Value("${simulation.request-per-interval}")
    private int requestPerInterval;

    @Value("${simulation.endpoint}")
    private String ingestionEndpoint;

    private final ExecutorService executor;

    public ParallelDataSimulator() {
        this.executor = Executors.newCachedThreadPool();
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Parallel Data Simulator started...");
        ((ThreadPoolExecutor) executor).setCorePoolSize(parallelThreads);
    }

    @Scheduled(fixedRateString = "${simulation.interval-ms}")
    public void sendMockData() {
        int batchSize = requestPerInterval / parallelThreads;
        int remainder = requestPerInterval % parallelThreads;

        for (int i = 0; i < parallelThreads; i++) {
            int requestForThread = batchSize + (i < remainder ? 1 : 0);
            executor.submit(() -> {
                for (int j = 0; j < requestForThread; j++) {
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
            });
        }
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
        log.info("Parallel Data Simulator shutdown...");
    }
}
