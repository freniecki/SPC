package spc.cloud.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
            String userId = UUID.randomUUID().toString();
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

            File file = new File(System.getProperty("java.io.tmpdir") + "/" + multipartFile.getOriginalFilename());
            multipartFile.transferTo(file);

            fileService.uploadFile(UUID.fromString(userId), file, multipartFile.getContentType());
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
            String userId = UUID.randomUUID().toString();
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

            List<File> files = multipartFiles.stream().map(multipartFile -> {
                try {
                    File tempFile = File.createTempFile("upload_", multipartFile.getOriginalFilename());
                    multipartFile.transferTo(tempFile);
                    return tempFile;
                } catch (IOException e) {
                    throw new RuntimeException("Failed to upload file: " + multipartFile.getOriginalFilename(), e);
                }
            }).collect(Collectors.toList());

            fileService.uploadFiles(UUID.fromString(userId), files, "application/octet-stream");
            return "File batch uploaded successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "File upload failed.";
        }
    }

    @PostMapping("/download/{fileName}")
    public String uploadFile(@AuthenticationPrincipal OAuth2User oauth2User, @PathVariable String fileName) {
        //String userId = oauth2User.getAttribute("sub");
        //TODO delete, only for test purposes
        String userId = UUID.randomUUID().toString();
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
        String downloadPath = System.getProperty("java.io.tmpdir") + "/" + fileName;
        fileService.downloadFile(UUID.fromString(userId), fileName, downloadPath);
        return "File downloaded to: " + downloadPath;
    }
}
