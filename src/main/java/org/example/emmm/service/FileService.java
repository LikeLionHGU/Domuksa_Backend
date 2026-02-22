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
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 없습니다");
        }

        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        boolean isPdf = false;

        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            isPdf = fileExtension.equalsIgnoreCase(".pdf");
        }

        // 1. S3 저장용 유니크한 이름 생성
        String uuidFileName = dirName + UUID.randomUUID() + fileExtension;

        // 2. 메타데이터 설정 (중요: PDF 및 이미지 타입 명시)
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());

        // PDF일 경우 브라우저 뷰어 호환성을 위해 타입을 강제 지정
        if (isPdf) {
            metadata.setContentType("application/pdf");
        } else {
            metadata.setContentType(file.getContentType());
        }

        // 3. S3 업로드 실행
        try {
            amazonS3Client.putObject(
                    new PutObjectRequest(bucket, uuidFileName, file.getInputStream(), metadata)
                            .withCannedAcl(CannedAccessControlList.PublicRead)
            );
        } catch (Exception e) {
            throw new RuntimeException("S3 업로드 중 오류가 발생했습니다: " + e.getMessage());
        }

        String s3Url = amazonS3Client.getUrl(bucket, uuidFileName).toString();

        // 4. DB 저장 및 연관 데이터 업데이트
        Agenda agenda = agendaRepository.findByIdAndDeletedFalse(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 안건입니다."));

        File f = File.builder()
                .agenda(agenda)
                .fileName(originalFileName)
                .fileUrl(s3Url)
                .s3Key(uuidFileName)
                .isPdf(isPdf)
                .build();

        // Config의 fileEnabled 업데이트 (Fetch Join 등을 고려하면 성능이 더 좋아집니다)
        AgendaConfig ac = agenda.getConfig();
        if (ac != null && Boolean.FALSE.equals(ac.getFileEnabled())) {
            ac.setFileEnabled(true);
        }

        File saved = fileRepository.save(f);
        return FileDto.CreateFileResDto.from(saved);
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



