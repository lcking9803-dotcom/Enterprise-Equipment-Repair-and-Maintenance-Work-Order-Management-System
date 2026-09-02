package com.lwq.maintenance.domain.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

public record InspectionActionRequest(
        boolean passed,
        @Size(max = 500) String remark) {
    @AssertTrue(message = "验收不通过时必须填写原因")
    public boolean isReasonValid() {
        return passed || (remark != null && !remark.isBlank());
    }
}

