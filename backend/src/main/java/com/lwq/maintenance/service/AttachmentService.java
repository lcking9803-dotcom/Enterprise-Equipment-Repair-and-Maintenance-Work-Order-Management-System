package com.lwq.maintenance.service;

import com.lwq.maintenance.auth.CurrentUser;
import com.lwq.maintenance.common.BusinessException;
import com.lwq.maintenance.domain.entity.Attachment;
import com.lwq.maintenance.mapper.AttachmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AttachmentService {
    private static final Set<String> STAGES = Set.of("REPORT", "REPAIR", "INSPECTION");
    private final AttachmentMapper attachmentMapper;
    private final WorkOrderService workOrderService;
    private final FileStorageService storageService;

    @Transactional
    public Attachment upload(Long workOrderId, String stage, MultipartFile file) throws Exception {
        workOrderService.accessible(workOrderId);
        String normalized = stage == null ? "REPORT" : stage.toUpperCase();
        if (!STAGES.contains(normalized)) throw new BusinessException(HttpStatus.BAD_REQUEST, "附件阶段不合法");
        var stored = storageService.store(file, workOrderId, normalized);
        Attachment attachment = new Attachment();
        attachment.setWorkOrderId(workOrderId);
        attachment.setStage(normalized);
        attachment.setOriginalName(stored.originalName());
        attachment.setObjectKey(stored.objectKey());
        attachment.setContentType(stored.contentType());
        attachment.setSizeBytes(stored.size());
        attachment.setUploaderId(CurrentUser.get().id());
        attachment.setCreatedAt(LocalDateTime.now());
        attachmentMapper.insert(attachment);
        return attachment;
    }

    public Download download(Long id) throws Exception {
        Attachment attachment = attachmentMapper.selectById(id);
        if (attachment == null) throw BusinessException.notFound("附件不存在");
        workOrderService.accessible(attachment.getWorkOrderId());
        return new Download(attachment, storageService.load(attachment.getObjectKey()));
    }

    public record Download(Attachment attachment, Resource resource) {}
}

