package com.lwq.maintenance.web;

import com.lwq.maintenance.common.ApiResponse;
import com.lwq.maintenance.domain.entity.Attachment;
import com.lwq.maintenance.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
public class AttachmentController {
    private final AttachmentService attachmentService;

    @PostMapping("/work-orders/{id}/attachments")
    public ApiResponse<Attachment> upload(@PathVariable Long id,
                                          @RequestParam(defaultValue = "REPORT") String stage,
                                          @RequestPart MultipartFile file) throws Exception {
        return ApiResponse.ok(attachmentService.upload(id, stage, file));
    }

    @GetMapping("/files/{id}")
    public ResponseEntity<org.springframework.core.io.Resource> download(@PathVariable Long id) throws Exception {
        var download = attachmentService.download(id);
        String encoded = URLEncoder.encode(download.attachment().getOriginalName(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.attachment().getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encoded)
                .body(download.resource());
    }
}

