package org.example.emmm.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;

import org.example.emmm.config.S3Config;
import org.example.emmm.dto.FileDto;
import org.example.emmm.repository.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.example.emmm.domain.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class FileService {
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3Client amazonS3Client;
    private final FileRepository fileRepository;

    public FileDto.CreateFileResDto uploadFile(MultipartFile file, String dirName)throws IOException {

        if (file == null||file.isEmpty()) {
            throw new IllegalArgumentException("파일이 없습니다");
        }
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            throw new IllegalArgumentException("파일 이름이 없습니다");
        }
        String fileExtension = "";//.png같은거
        if(originalFileName.contains(".")){
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String uuidFileName = dirName + UUID.randomUUID() + fileExtension;//이름 랜덤,dirName은 S3 버킷 안의 “폴더 경로”

        ObjectMetadata metadata = new ObjectMetadata();//S3에 저장될 부가 정보(헤더)
        metadata.setContentLength(file.getSize());//파일 크기
        metadata.setContentType(file.getContentType());//image/png application/pdf 등 type 정해줌

        amazonS3Client.putObject(
                new PutObjectRequest(bucket, uuidFileName, file.getInputStream(), metadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead)
        );

        String s3Url = amazonS3Client.getUrl(bucket, uuidFileName).toString();//s3가 준 Url저장

        File f = File.builder()
                .agendaId(null)
                .fileName(originalFileName)
                .fileUrl(s3Url)
                .s3Key(uuidFileName)
                .build();

        File saved = fileRepository.save(f);
        return FileDto.CreateFileResDto.from(saved);//s3에 파일 저장



    }
    public List<FileDto.FileListResDto> getFile(Long agendaId){
        return fileRepository.findByAgendaId(agendaId)
                .stream()
                .map(FileDto.FileListResDto::from)
                .toList();


    }
}

