package org.example.emmm.controller;

import lombok.RequiredArgsConstructor;
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
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileDto.CreateFileResDto> postFile(@RequestPart("file") MultipartFile file) throws IOException {
        FileDto.CreateFileResDto response = fileService.uploadFile(file, "domuksa/");
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<FileDto.FileListResDto>> getFile(@RequestParam Long agendaId){
        List<FileDto.FileListResDto> files = fileService.getFile(agendaId);
        return ResponseEntity.ok(files);
    }
}
