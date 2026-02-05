package org.zr.device_service.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.zr.device_service.model.DeviceType;

@Entity
@Table(name = "devices")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private DeviceType type;

    private String location;
    
    private Long userId;
}
