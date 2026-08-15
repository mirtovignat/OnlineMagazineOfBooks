package com.example.demo.service.movie.poster;

import com.example.demo.config.MinIOConfig;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PosterService {

    private final MinioClient minioClient;
    private final MinIOConfig minIOConfig;

    public String uploadPosterFromUrl(String imageUrl, String fileName) throws Exception {
        RestClient restClient = RestClient.create();
        Resource resource = restClient.get()
                .uri(imageUrl)
                .retrieve()
                .body(Resource.class);
        if (resource == null) {
            throw new IllegalStateException("Не удалось скачать постер: " + imageUrl);
        }
        try (InputStream inputStream = resource.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minIOConfig.getBucketName())
                            .object(fileName)
                            .stream(inputStream, resource.contentLength(), -1)
                            .contentType("image/jpeg")
                            .build()
            );
        }
        return getPublicUrl(fileName);
    }

    public String getPublicUrl(String fileName) {
        return minIOConfig.getExternalEndpoint()
                + "/" + minIOConfig.getBucketName()
                + "/" + fileName;
    }

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

}