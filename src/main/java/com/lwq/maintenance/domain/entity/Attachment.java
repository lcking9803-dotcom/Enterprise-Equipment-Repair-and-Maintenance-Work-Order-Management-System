package com.lwq.maintenance.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("attachment")
public class Attachment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long workOrderId;
    private String stage;
    private String originalName;
    private String objectKey;
    private String contentType;
    private Long sizeBytes;
    private Long uploaderId;
    private LocalDateTime createdAt;
}

