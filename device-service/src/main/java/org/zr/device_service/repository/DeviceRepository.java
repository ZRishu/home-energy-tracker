package org.zr.device_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zr.device_service.domain.entity.Device;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
}
