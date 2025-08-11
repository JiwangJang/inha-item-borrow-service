package com.inha.borrow.backend.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3StorageConfig {
    @Value("${app.cloud.aws.s3.credentials.accessKey}")
    private String accessKey;
    @Value("${app.cloud.aws.s3.credentials.secretKey}")
    private String secretKey;
    @Value("${app.cloud.aws.s3.region}")
    private String region;

    @Bean
    AmazonS3 s3Client() {
        BasicAWSCredentials aswCreds = new BasicAWSCredentials(accessKey, secretKey);
        return (AmazonS3Client) AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(aswCreds))
                .build();
    }
}
