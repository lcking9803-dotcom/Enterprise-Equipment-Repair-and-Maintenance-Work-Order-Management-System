package com.lwq.maintenance.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DispatchActionRequest(
        @NotNull(message = "维修人员不能为空") Long assigneeId,
        @Size(max = 500) String remark) {
}

