package org.example.emmm.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;

import org.example.emmm.config.S3Config;
import org.example.emmm.domain.Agenda;
import org.example.emmm.domain.AgendaConfig;
import org.example.emmm.dto.FileDto;
import org.example.emmm.repository.AgendaConfigRepository;
import org.example.emmm.repository.AgendaRepository;
import org.example.emmm.repository.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.example.emmm.domain.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class FileService {
    private final AgendaConfigRepository agendaConfigRepository;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3Client amazonS3Client;
    private final FileRepository fileRepository;
    private final AgendaRepository agendaRepository;

    //Todo: parameter controller에서 바뀐대로 수정 + agendaId로 agenda 해당 agenda 불러오기 + agenda(null) 이거 null에 agenda로 바꾸기
    @Transactional
    public FileDto.CreateFileResDto uploadFile(MultipartFile file, String dirName, Long agendaId) throws IOException {

        if (file == null||file.isEmpty()) {
            throw new IllegalArgumentException("파일이 없습니다");
        }
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            throw new IllegalArgumentException("파일 이름이 없습니다");
        }
        String fileExtension = "";//.png같은거
        boolean isPdf = false;
        if(originalFileName.contains(".")){
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            isPdf = fileExtension.equalsIgnoreCase(".pdf");
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
        Agenda agenda = agendaRepository.findByIdAndDeletedFalse(agendaId).orElseThrow();
        File f = File.builder()
                .agenda(agenda)
                .fileName(originalFileName)
                .fileUrl(s3Url)
                .s3Key(uuidFileName)
                .isPdf(isPdf)
                .build();

        AgendaConfig ac = agendaConfigRepository.findByIdAndDeletedFalse(agenda.getId()).orElseThrow();

        //fileEnabled이 false면 true로 바꿔줌
        if(ac.getFileEnabled().equals(false)) {
            ac.setFileEnabled(true);
        }

        File saved = fileRepository.save(f);
        return FileDto.CreateFileResDto.from(saved);//s3에 파일 저장
    }
    public List<FileDto.FileListResDto> getFile(Long agendaId){
        return fileRepository.findByAgendaId(agendaId)
                .stream()
                .map(FileDto.FileListResDto::from)
                .toList();
    }

    public File deletedFile (Long fileId){
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("해당 파일이 존재하지 않습니다. id=" + fileId));

        try {
            amazonS3Client.deleteObject(bucket,file.getS3Key());
        } catch (Exception e) {
            throw new RuntimeException("S3 파일 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
        fileRepository.delete(file);

        return file;
    }

}



