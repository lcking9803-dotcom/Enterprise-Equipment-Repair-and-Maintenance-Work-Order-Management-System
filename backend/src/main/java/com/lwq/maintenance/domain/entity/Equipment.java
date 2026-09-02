package com.lwq.maintenance.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.lwq.maintenance.domain.enums.EquipmentStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("equipment")
public class Equipment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String equipmentCode;
    private String name;
    private String category;
    private String location;
    private String department;
    private String responsiblePerson;
    private EquipmentStatus status;
    private Integer maintenanceCycleDays;
    private LocalDate lastMaintenanceDate;
    private LocalDate nextMaintenanceDate;
    private String description;
    @Version
    private Integer version;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

