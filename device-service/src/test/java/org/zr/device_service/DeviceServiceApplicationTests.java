package org.zr.device_service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.zr.device_service.domain.entity.Device;
import org.zr.device_service.model.DeviceType;
import org.zr.device_service.repository.DeviceRepository;

@Slf4j
@SpringBootTest
class DeviceServiceApplicationTests {

    public static final int NUMBER_OF_DEVICES = 200;
    public static final int USERS = 10;
    @Autowired
    private DeviceRepository deviceRepository;

    @Test
    void contextLoads() {
    }

    @Disabled
    @Test
    void createDevice() {
        for (int i = 1; i <= NUMBER_OF_DEVICES; i++) {
            Device device = Device.builder()
                    .name("device" + i)
                    .type(DeviceType.values()[i % DeviceType.values().length])
                    .location("location" + ((i % 3) + 1))
                    .userId((long) ((i % USERS) + 1))
                    .build();
            deviceRepository.save(device);
        }
        log.info("Device repository has been populated");
    }

}
