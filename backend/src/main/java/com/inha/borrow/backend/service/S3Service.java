package com.inha.borrow.backend.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {
    private final AmazonS3 amazonS3;

    @Value("${app.cloud.aws.s3.bucket}")
    private String bucket;

    /**
     *
     * @param multipartFile
     * @param id
     * @param folder
     * @return 사진이 저장된 url
     * @author 형민재
     */
    public String uploadFile(MultipartFile multipartFile, String folder, String id) {
        String originalFilename = multipartFile.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = folder + "/" + id + extension;
        ObjectMetadata objMeta = new ObjectMetadata();
        objMeta.setContentLength(multipartFile.getSize());
        objMeta.setContentType(multipartFile.getContentType());
        try {
            amazonS3.putObject(bucket, fileName, multipartFile.getInputStream(), objMeta);
        } catch (IOException e) {
            throw new AmazonS3Exception("s3 업로드 실패");
        }
        return amazonS3.getUrl(bucket, fileName).toString();
    }
    public void deleteFile(String bucket,String folder, String id){
        String key = folder+"/"+id;
        amazonS3.deleteObject(bucket,key);
    }
    public void deleteAllFile(String bucket ,String id){
        amazonS3.deleteObject(bucket,"student-council-fee/"+id);
        amazonS3.deleteObject(bucket,"student-identification/"+id);
    }
}
