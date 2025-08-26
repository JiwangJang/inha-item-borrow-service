package com.inha.borrow.backend.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

class S3ServiceTest {

    @Mock
    private AmazonS3 amazonS3;

    @InjectMocks
    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        amazonS3 = mock(AmazonS3.class);
        s3Service = new S3Service(amazonS3);

        Field bucketField = null;
        try {
            bucketField = S3Service.class.getDeclaredField("bucket");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        bucketField.setAccessible(true);
        try {
            bucketField.set(s3Service, "test-bucket");
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void uploadFile_shouldUploadAndReturnUrl() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("photo.jpg");
        when(file.getSize()).thenReturn(10L);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[] { 1, 2, 3 }));

        when(amazonS3.getUrl(anyString(), anyString())).thenReturn(any());

        String result = s3Service.uploadFile(file, "folder", "123");

        assertThat(result).isEqualTo("http://mock-url/photo.jpg");

        ArgumentCaptor<ObjectMetadata> metadataCaptor = ArgumentCaptor.forClass(ObjectMetadata.class);
        verify(amazonS3).putObject(eq("test-bucket"), eq("folder/123.jpg"), any(), metadataCaptor.capture());

        ObjectMetadata metadata = metadataCaptor.getValue();
        assertThat(metadata.getContentLength()).isEqualTo(10L);
        assertThat(metadata.getContentType()).isEqualTo("image/jpeg");
    }

    @Test
    void deleteFile_shouldCallAmazonS3Delete() {
        s3Service.deleteFile("test-bucket", "folder", "123");
        verify(amazonS3).deleteObject("test-bucket", "folder/123");
    }

    @Test
    void deleteAllFile_shouldCallAmazonS3DeleteForBothFolders() {
        s3Service.deleteAllFile("test-bucket", "123");
        verify(amazonS3).deleteObject("test-bucket", "student-council-fee/123");
        verify(amazonS3).deleteObject("test-bucket", "student-identification/123");
    }
}
