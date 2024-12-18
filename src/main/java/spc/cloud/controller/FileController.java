package spc.cloud.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import spc.cloud.service.FileService;

import java.io.File;
import java.util.UUID;

@RestController
@RequestMapping("/files")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("userId") UUID userId, @RequestParam("file") MultipartFile multipartFile) {
        try {
            File file = new File(System.getProperty("java.io.tmpdir") + "/" + multipartFile.getOriginalFilename());
            multipartFile.transferTo(file);

            fileService.uploadFile(userId, file, multipartFile.getContentType());
            return "File uploaded successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "File upload failed.";
        }
    }
}
