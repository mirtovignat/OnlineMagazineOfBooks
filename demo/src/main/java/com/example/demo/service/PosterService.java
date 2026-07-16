package com.example.demo.service;

import com.example.demo.config.MinIOConfig;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PosterService {

    private final MinioClient minioClient;
    private final MinIOConfig minIOConfig;

    public String uploadPoster(MultipartFile file) throws Exception {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(minIOConfig.getBucketName())
                        .object(fileName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
        return minIOConfig.getExternalEndpoint() + "/" + minIOConfig.getBucketName() + "/" + fileName;
    }

    public void deletePoster(String posterUrl) throws Exception {
        String fileName = posterUrl.substring(posterUrl.lastIndexOf('/') + 1);
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(minIOConfig.getBucketName())
                        .object(fileName)
                        .build()
        );
    }
}