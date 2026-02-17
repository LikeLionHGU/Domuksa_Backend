package org.example.emmm.controller;

import lombok.RequiredArgsConstructor;
import org.example.emmm.domain.Agenda;
import org.example.emmm.dto.FileDto;
import org.example.emmm.service.FileService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    //Todo: agendaId 받아오기 + service의 parameter로 받기
    @PostMapping(value = "/{agendaId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileDto.CreateFileResDto> postFile(@PathVariable Long agendaId, @RequestPart("file") MultipartFile file) throws IOException {
        FileDto.CreateFileResDto response = fileService.uploadFile(file, "domuksa/", agendaId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{agendaId}")
    public ResponseEntity<List<FileDto.FileListResDto>> getFile(@PathVariable Long agendaId){
        List<FileDto.FileListResDto> files = fileService.getFile(agendaId);
        return ResponseEntity.ok(files);
    }
}
