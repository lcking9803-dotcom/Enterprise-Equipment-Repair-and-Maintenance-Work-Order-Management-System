package com.lwq.maintenance.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RepairActionRequest(
        @NotBlank(message = "维修说明不能为空") @Size(max = 2000) String repairDescription,
        @DecimalMin(value = "0.00", message = "维修费用不能为负数") BigDecimal repairCost,
        @Size(max = 500) String remark) {
}

