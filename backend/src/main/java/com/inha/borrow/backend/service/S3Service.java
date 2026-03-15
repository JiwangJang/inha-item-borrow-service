package com.inha.borrow.backend.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {
    private final AmazonS3 amazonS3;

    @Value("${app.cloud.aws.s3.bucket}")
    private String bucket;

    /**
     *
     * @param multipartFile 파일
     * @param folder        폴더
     * @param name          파일명
     * @return 사진이 저장된 url
     * @author 형민재
     */
    public String uploadFile(MultipartFile multipartFile, String folder) {
        String originalFilename = multipartFile.getOriginalFilename();
        String extension = "";
        String uuid = UUID.randomUUID().toString().replace("-", "");
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = folder + "/" + uuid + extension;
        ObjectMetadata objMeta = new ObjectMetadata();
        objMeta.setContentLength(multipartFile.getSize());
        objMeta.setContentType(multipartFile.getContentType());
        try {
            amazonS3.putObject(bucket, fileName, multipartFile.getInputStream(), objMeta);
        } catch (IOException e) {
            throw new AmazonS3Exception("s3 업로드 실패");
        }
        amazonS3.getUrl(bucket, fileName).toString();
        return fileName;
    }

    public String getPresignedUrl(String key) {
        Date expiration = new Date(System.currentTimeMillis() + 1000 * 60); // 1분

        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, key)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);

        URL url = amazonS3.generatePresignedUrl(request);
        return url.toString();
    }

    /**
     * S3에 저장된 사진 삭제
     * 
     * @param path 저장된 경로(폴더+이름)
     */
    public void deleteFile(String path) {
        try {
            amazonS3.deleteObject(bucket, path);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
