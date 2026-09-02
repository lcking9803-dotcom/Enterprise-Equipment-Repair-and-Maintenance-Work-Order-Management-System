package com.lwq.maintenance.domain.dto;

import com.lwq.maintenance.domain.entity.Attachment;
import com.lwq.maintenance.domain.entity.WorkOrder;
import com.lwq.maintenance.domain.entity.WorkOrderLog;

import java.util.List;

public record WorkOrderDetail(WorkOrder workOrder, List<WorkOrderLog> logs, List<Attachment> attachments) {
}

