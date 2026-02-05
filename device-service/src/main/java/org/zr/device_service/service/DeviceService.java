package org.zr.device_service.service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.zr.device_service.domain.dto.DeviceDto;
import org.zr.device_service.domain.entity.Device;
import org.zr.device_service.exception.DeviceNotFoundException;
import org.zr.device_service.repository.DeviceRepository;

@Service
public class DeviceService {
    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public DeviceDto getDeviceById(Long id) {
        Device device = deviceRepository
                .findById(id).orElseThrow(() -> new DeviceNotFoundException("Device not found with id:" + id));
        return mapToDto(device);
    }

    public DeviceDto createDevice(DeviceDto input) {
        Device device = new Device();
        device.setId(input.id());
        device.setName(input.name());
        device.setType(input.type());
        device.setLocation(input.location());
        device.setUserId(input.userId());

        Device savedDevice = deviceRepository.save(device);
        return mapToDto(savedDevice);
    }

    public DeviceDto updateDevice(@PathVariable long id, @RequestBody DeviceDto input) {
        Device existingDevice = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found with id:" + id));
        existingDevice.setName(input.name());
        existingDevice.setType(input.type());
        existingDevice.setLocation(input.location());
        existingDevice.setUserId(input.userId());

        Device savedDevice = deviceRepository.save(existingDevice);
        return mapToDto(savedDevice);
    }

    public void deleteDevice(@PathVariable long id) {
        if (!deviceRepository.existsById(id)) {
            throw new DeviceNotFoundException("Device not found with id:" + id);
        }
        deviceRepository.deleteById(id);
    }

    private DeviceDto mapToDto(Device device) {
        return DeviceDto.builder()
                .id(device.getId())
                .name(device.getName())
                .type(device.getType())
                .location(device.getLocation())
                .userId(device.getUserId())
                .build();
    }
}
