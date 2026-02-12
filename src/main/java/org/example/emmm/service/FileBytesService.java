package org.example.emmm.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class FileBytesService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public byte[] downloadByS3Key(String s3Key) {
        try (S3Object obj = amazonS3.getObject(bucket, s3Key);
             InputStream is = obj.getObjectContent();
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {

            byte[] buf = new byte[8192];
            int read;
            while ((read = is.read(buf)) != -1) os.write(buf, 0, read);
            return os.toByteArray();

        } catch (Exception e) {
            throw new IllegalStateException("S3 다운로드 실패: key=" + s3Key, e);
        }
    }
}
