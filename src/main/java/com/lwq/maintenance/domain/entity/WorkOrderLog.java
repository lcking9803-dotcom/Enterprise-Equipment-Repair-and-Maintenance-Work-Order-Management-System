package com.lwq.maintenance.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lwq.maintenance.domain.enums.WorkOrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("work_order_log")
public class WorkOrderLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long workOrderId;
    private WorkOrderStatus fromStatus;
    private WorkOrderStatus toStatus;
    private String action;
    private Long operatorId;
    private String operatorName;
    private String remark;
    private LocalDateTime createdAt;
}

