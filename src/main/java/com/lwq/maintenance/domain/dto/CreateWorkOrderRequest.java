package com.lwq.maintenance.domain.dto;

import com.lwq.maintenance.domain.enums.Priority;
import jakarta.validation.constraints.*;

public record CreateWorkOrderRequest(
        @NotNull(message = "设备不能为空") Long equipmentId,
        @NotBlank(message = "工单标题不能为空") @Size(max = 120) String title,
        @NotBlank(message = "故障类型不能为空") @Size(max = 60) String faultType,
        @NotBlank(message = "故障描述不能为空") @Size(max = 2000) String faultDescription,
        @NotNull(message = "优先级不能为空") Priority priority) {
}

