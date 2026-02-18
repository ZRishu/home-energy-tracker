package org.zr.usage_service.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.zr.kafka.event.EnergyUsageEvent;
import org.zr.usage_service.client.DeviceClient;
import org.zr.usage_service.client.UserClient;
import org.zr.usage_service.dto.DeviceDto;
import org.zr.usage_service.dto.UserDto;
import org.zr.usage_service.model.DeviceEnergy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UsageService {

    private final InfluxDBClient influxDBClient;
    private final DeviceClient deviceClient;
    private final UserClient userClient;

    @Value("${influx.bucket}")
    private String influxBucket;

    @Value("${influx.org}")
    private String influxOrg;

    public UsageService(InfluxDBClient influxDBClient, DeviceClient deviceClient, UserClient userClient) {
        this.influxDBClient = influxDBClient;
        this.deviceClient = deviceClient;
        this.userClient = userClient;
    }

    @KafkaListener(topics = "energy-usage", groupId = "usage-service")
    public void energyUsageEvent(EnergyUsageEvent energyUsageEvent) {
        log.info("Recieved energy usage event: {}", energyUsageEvent);

        Point point = Point.measurement("energy-usage")
                .addTag("deviceId", String.valueOf(energyUsageEvent.deviceId()))
                .addField("energyConsumed", energyUsageEvent.energyConsumed())
                .time(energyUsageEvent.timestamp(), WritePrecision.MS);

        influxDBClient.getWriteApiBlocking().writePoint(influxBucket, influxOrg, point);
    }

    @Scheduled(cron = "*/10 * * * * *")
    public void aggregateDeviceEnergyUsage() {
        final Instant now = Instant.now();
        final Instant oneHourAgo = now.minusSeconds(3600);

        String fluxQuery = String.format("""
                from(bucket: "%s")
                    |> range(start: time(v: "%s"), stop: time(v: "%s"))
                    |> filter(fn: (r) => r["_measurement"] == "energy_usage")
                    |> filter(fn: (r) => r["_field"] == "energyConsumed")
                    |> group(columns: ["deviceId"])
                    |> sum(column: "_value")
                """, influxBucket, oneHourAgo.toString(), now);

        QueryApi queryApi = influxDBClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(fluxQuery, influxOrg);

        List<DeviceEnergy> deviceEnergies = new ArrayList<>();

        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                String deviceIdStr = (String) record.getValueByKey("deviceId");
                Double energyConsumed = record.getValueByKey("_value") instanceof Number ?
                        ((Number) record.getValueByKey("_value")).doubleValue() : 0.0;

                deviceEnergies.add(DeviceEnergy.builder()
                        .deviceId(Long.valueOf(deviceIdStr))
                        .energyConsumed(energyConsumed)
                        .build()
                );
            }
        }
        log.info("Aggregated device energies over the past hour: {}", deviceEnergies);

        for (DeviceEnergy deviceEnergy : deviceEnergies) {
            DeviceDto deviceResponse = deviceClient.getDeviceById(deviceEnergy.getDeviceId());
            if (deviceResponse == null || deviceResponse.id() == null) {
                log.warn("Device not found with ID: {}", deviceEnergy.getDeviceId());
                continue;
            }
            deviceEnergy.setDeviceId(deviceResponse.id());
        }

        // Remove devices with null userId
        deviceEnergies.removeIf(deviceEnergy -> deviceEnergy.getUserId() == null);

        // Get user-device mapping and aggregate per user
        Map<Long, List<DeviceEnergy>> userDeviceEnergyMap =
                deviceEnergies.stream()
                        .collect(Collectors.groupingBy(DeviceEnergy::getUserId));

        log.info("User-Device Energy Map: {}", userDeviceEnergyMap);

        // Get user energy consumption threshold
        List<Long> userIds = new ArrayList<>(userDeviceEnergyMap.keySet());
        final Map<Long, Double> userThresholdMap = new HashMap<>();
        final Map<Long, String> userEmailMap = new HashMap<>();

        for (Long userId : userIds) {
            try {
                UserDto user = userClient.getUserById(userId);
                if (user == null || user.id() == null || !user.alerting()) {
                    log.warn("User not found or alerting disabled for ID: {}", userId);
                    continue;
                }
                userThresholdMap.put(userId, user.energyAlertingThreshold());
                userEmailMap.put(userId, user.email());
            } catch (Exception e) {
                log.warn("Failed to fetch user for ID: {}", userId);
            }
        }
        log.info("User Threshold Map: {}", userThresholdMap);
    }
}
