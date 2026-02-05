package org.zr.device_service.domain.dto;

import lombok.Builder;
import org.zr.device_service.model.DeviceType;

@Builder
public record DeviceDto(
        Long id,
        String name,
        DeviceType type,
        String location,
        Long userId
) {
}
