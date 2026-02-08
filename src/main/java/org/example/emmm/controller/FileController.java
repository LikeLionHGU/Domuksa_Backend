package org.example.emmm.controller;

import lombok.RequiredArgsConstructor;
import org.example.emmm.dto.FileDto;
import org.example.emmm.service.FileService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileDto.CreateFileResDto> postFile(@RequestPart("file") MultipartFile file) throws IOException {
        FileDto.CreateFileResDto response = fileService.uploadFile(file, "domuksa/");
        return ResponseEntity.ok(response);
    }
}
