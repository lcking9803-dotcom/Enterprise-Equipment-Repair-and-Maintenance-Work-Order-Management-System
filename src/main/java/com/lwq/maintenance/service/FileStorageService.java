package com.lwq.maintenance.service;

import com.lwq.maintenance.common.BusinessException;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "application/pdf");
    private final String type;
    private final Path localDir;
    private final String bucket;
    private final MinioClient minioClient;

    public FileStorageService(@Value("${storage.type}") String type,
                              @Value("${storage.local-dir}") String localDir,
                              @Value("${storage.endpoint}") String endpoint,
                              @Value("${storage.access-key}") String accessKey,
                              @Value("${storage.secret-key}") String secretKey,
                              @Value("${storage.bucket}") String bucket) {
        this.type = type;
        this.localDir = Paths.get(localDir).toAbsolutePath().normalize();
        this.bucket = bucket;
        this.minioClient = MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
    }

    @PostConstruct
    public void initialize() throws Exception {
        if ("minio".equalsIgnoreCase(type)) {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        } else {
            Files.createDirectories(localDir);
        }
    }

    public StoredFile store(MultipartFile file, Long workOrderId, String stage) throws Exception {
        if (file.isEmpty()) throw new BusinessException(HttpStatus.BAD_REQUEST, "文件不能为空");
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "只允许 JPG、PNG、WEBP 或 PDF 文件");
        }
        String extension = safeExtension(file.getOriginalFilename());
        String objectKey = "work-orders/" + workOrderId + "/" + stage.toLowerCase(Locale.ROOT) + "/" + UUID.randomUUID() + extension;
        if ("minio".equalsIgnoreCase(type)) {
            minioClient.putObject(PutObjectArgs.builder().bucket(bucket).object(objectKey)
                    .stream(file.getInputStream(), file.getSize(), -1).contentType(file.getContentType()).build());
        } else {
            Path target = localDir.resolve(objectKey).normalize();
            if (!target.startsWith(localDir)) throw new BusinessException(HttpStatus.BAD_REQUEST, "非法文件路径");
            Files.createDirectories(target.getParent());
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        return new StoredFile(objectKey, StringUtils.cleanPath(file.getOriginalFilename()), file.getContentType(), file.getSize());
    }

    public Resource load(String objectKey) throws Exception {
        if ("minio".equalsIgnoreCase(type)) {
            InputStream stream = minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(objectKey).build());
            return new InputStreamResource(stream);
        }
        Path target = localDir.resolve(objectKey).normalize();
        if (!target.startsWith(localDir) || !Files.exists(target)) throw BusinessException.notFound("附件不存在");
        return new FileSystemResource(target);
    }

    private String safeExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) return "";
        String ext = filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        return Set.of(".jpg", ".jpeg", ".png", ".webp", ".pdf").contains(ext) ? ext : "";
    }

    public record StoredFile(String objectKey, String originalName, String contentType, long size) {}
}

