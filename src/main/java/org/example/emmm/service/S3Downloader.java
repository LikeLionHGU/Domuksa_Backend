package org.example.emmm.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class S3Downloader {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3Client amazonS3Client;

    /**
     * @param s3Key File 엔티티에 저장한 s3Key (예: "domuksa/uuid.pdf")
     */
    public byte[] downloadByKey(String s3Key) {
        if (s3Key == null || s3Key.isBlank()) {
            throw new IllegalArgumentException("s3Key is empty");
        }

        S3Object obj = amazonS3Client.getObject(bucket, s3Key);
        try (S3ObjectInputStream in = obj.getObjectContent()) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to download from S3: " + s3Key, e);
        }
    }
}
