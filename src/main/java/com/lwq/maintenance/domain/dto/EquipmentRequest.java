package com.lwq.maintenance.domain.dto;

import com.lwq.maintenance.domain.enums.EquipmentStatus;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record EquipmentRequest(
        @NotBlank(message = "设备编号不能为空") @Size(max = 40) String equipmentCode,
        @NotBlank(message = "设备名称不能为空") @Size(max = 100) String name,
        @NotBlank(message = "设备分类不能为空") @Size(max = 50) String category,
        @NotBlank(message = "设备位置不能为空") @Size(max = 120) String location,
        @NotBlank(message = "责任部门不能为空") @Size(max = 80) String department,
        @Size(max = 50) String responsiblePerson,
        @NotNull(message = "设备状态不能为空") EquipmentStatus status,
        @Min(value = 1, message = "保养周期至少1天") @Max(value = 3650, message = "保养周期不能超过3650天") Integer maintenanceCycleDays,
        LocalDate lastMaintenanceDate,
        @Size(max = 500) String description) {
}

