package org.example.emmm.controller;

import lombok.RequiredArgsConstructor;
import org.example.emmm.domain.File;
import org.example.emmm.dto.FileDto;
import org.example.emmm.service.FileService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;
    private final SimpMessagingTemplate template;

    public String createWsRes(String text){
        return text+ LocalDateTime.now();
    }

    @PostMapping(value = "/{agendaId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileDto.CreateFileResDto> postFile(@PathVariable Long agendaId, @RequestPart("file") MultipartFile file) throws IOException {
        FileDto.CreateFileResDto res = fileService.uploadFile(file, "domuksa/", agendaId);

        String wsRes = createWsRes("update webSocket");

        template.convertAndSend("/topic/file/list/"+res.getAgendaId(), wsRes);

        return ResponseEntity.ok(res);
    }

    @GetMapping("/{agendaId}")
    public ResponseEntity<List<FileDto.FileListResDto>> getFile(@PathVariable Long agendaId){
        List<FileDto.FileListResDto> files = fileService.getFile(agendaId);
        String wsRes = createWsRes("update webSocket");
        template.convertAndSend("/topic/file/list/"+agendaId, wsRes);
        return ResponseEntity.ok(files);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<String> deleteFile(@PathVariable Long fileId) {
        File f = fileService.deletedFile(fileId);

        String wsRes = createWsRes("update webSocket");

        template.convertAndSend("/topic/file/list/"+f.getAgenda().getId(), wsRes);
        return ResponseEntity.ok("파일이 성공적으로 삭제되었습니다.");
}

}
