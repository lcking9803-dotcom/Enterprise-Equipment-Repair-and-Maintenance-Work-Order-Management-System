package com.lwq.maintenance.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.lwq.maintenance.domain.enums.Priority;
import com.lwq.maintenance.domain.enums.WorkOrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("work_order")
public class WorkOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long equipmentId;
    private String title;
    private String faultType;
    private String faultDescription;
    private Priority priority;
    private WorkOrderStatus status;
    private Long reporterId;
    private Long dispatcherId;
    private Long assigneeId;
    private Long inspectorId;
    private String repairDescription;
    private BigDecimal repairCost;
    private String rejectionReason;
    private LocalDateTime acceptedAt;
    private LocalDateTime dispatchedAt;
    private LocalDateTime repairedAt;
    private LocalDateTime closedAt;
    private LocalDateTime slaDeadline;
    @Version
    private Integer version;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

