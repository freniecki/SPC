package spc.cloud.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import spc.cloud.entity.User;
import spc.cloud.entity.UserFile;
import spc.cloud.repository.UserRepository;
import spc.cloud.service.FileService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/files")
public class FileController {
    private final FileService fileService;
    @Autowired
    private UserRepository userRepository;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public String uploadFile(@AuthenticationPrincipal OAuth2User oauth2User, @RequestParam("file") MultipartFile multipartFile) {
        try {
            //String userId = oauth2User.getAttribute("sub");
            //TODO delete, only for test purposes
            String userId = "d3a59249-bac8-4711-a47b-76778d18fcb5";
            Optional<User> userOptional = userRepository.findById(UUID.fromString(userId));
            User user = userOptional.orElse(null);
            if (user == null) {
                user = new User();
                user.setUserId(UUID.fromString(userId));
                user.setEmail("email");
                user.setName("username");
                userRepository.save(user);
            }
            //todo delete code above

            fileService.uploadFile(
                    UUID.fromString(userId),
                    multipartFile.getInputStream(),
                    multipartFile.getOriginalFilename(),
                    multipartFile.getContentType(),
                    multipartFile.getSize());
            return "File uploaded successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "File upload failed.";
        }
    }

    @PostMapping("/upload/batch")
    public String uploadFileBatch(@AuthenticationPrincipal OAuth2User oauth2User, @RequestParam("files") List<MultipartFile> multipartFiles) {
        try {
            //String userId = oauth2User.getAttribute("sub");
            //TODO delete, only for test purposes
            String userId = "d3a59249-bac8-4711-a47b-76778d18fcb5";
            Optional<User> userOptional = userRepository.findById(UUID.fromString(userId));
            User user = userOptional.orElse(null);
            if (user == null) {
                user = new User();
                user.setUserId(UUID.fromString(userId));
                user.setEmail("email");
                user.setName("username");
                userRepository.save(user);
            }
            //todo delete code above

            multipartFiles.forEach(multipartFile -> {
                try {
                    fileService.uploadFile(
                            UUID.fromString(userId),
                            multipartFile.getInputStream(),
                            multipartFile.getOriginalFilename(),
                            multipartFile.getContentType(),
                            multipartFile.getSize());
                } catch (IOException e) {
                    throw new RuntimeException("Failed to upload file: " + multipartFile.getOriginalFilename(), e);
                }
            });

            return "File batch uploaded successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "File upload failed.";
        }
    }

    @PostMapping("/download/{fileName}")
    public ResponseEntity<Resource> uploadFile(@AuthenticationPrincipal OAuth2User oauth2User, @PathVariable String fileName) {
        //String userId = oauth2User.getAttribute("sub");
        //TODO delete, only for test purposes
        String userId = "d3a59249-bac8-4711-a47b-76778d18fcb5";
        Optional<User> userOptional = userRepository.findById(UUID.fromString(userId));
        User user = userOptional.orElse(null);
        if (user == null) {
            user = new User();
            user.setUserId(UUID.fromString(userId));
            user.setEmail("email");
            user.setName("username");
            userRepository.save(user);
        }
        //todo delete code above
        try {
            // Download file to a temporary path
            String tempFilePath = System.getProperty("java.io.tmpdir") + "/" + UUID.randomUUID() + "_" + fileName;
            File downloadedFile = fileService.downloadFile(UUID.fromString(userId), fileName, tempFilePath);

            // Prepare file resource for download
            Path filePath = downloadedFile.toPath();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("Failed to download file: " + fileName);
            }

            // Set headers for file download
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to download file: " + fileName, e);
        }
    }

    @GetMapping("/list")
    public List<UserFile> listUserFiles(@AuthenticationPrincipal OAuth2User oauth2User) {
        //String userId = oauth2User.getAttribute("sub");
        //TODO replace with actual authenticated userId
        String userId = "d3a59249-bac8-4711-a47b-76778d18fcb5";
        Optional<User> userOptional = userRepository.findById(UUID.fromString(userId));
        User user = userOptional.orElse(null);
        if (user == null) {
            user = new User();
            user.setUserId(UUID.fromString(userId));
            user.setEmail("email");
            user.setName("username");
            userRepository.save(user);
        }

        return fileService.getUserFiles(UUID.fromString(userId));
    }

}
