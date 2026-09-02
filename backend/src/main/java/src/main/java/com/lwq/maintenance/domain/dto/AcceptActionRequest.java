package com.lwq.maintenance.domain.dto;

import jakarta.validation.constraints.Size;

public record AcceptActionRequest(@Size(max = 500) String remark) {
}

