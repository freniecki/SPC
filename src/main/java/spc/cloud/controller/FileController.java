package spc.cloud.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/files")
public class FileController {
    private final FileService fileService;
    @Autowired
    private UserRepository userRepository;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }


    @Operation(summary = "Upload file", description = "User uploads one file to the system", tags = "Files")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File was uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "File upload failed")
    })
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@AuthenticationPrincipal OAuth2User oauth2User, @RequestParam("file") MultipartFile multipartFile) {
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
            return ResponseEntity.ok("File uploaded successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body("File upload failed.");
        }
    }

    @Operation(summary = "Upload files in batch", description = "User uploads many files in a batch to the system", tags = "Files")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "All files were uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "File upload failed")
    })
    @PostMapping("/upload/batch")
    public ResponseEntity<?> uploadFileBatch(@AuthenticationPrincipal OAuth2User oauth2User, @RequestParam("files") List<MultipartFile> multipartFiles) {
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

            return ResponseEntity.status(303).body("File batch uploaded on the client side successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body("File upload failed.");
        }
    }

    @Operation(summary = "Download file", description = "User downloads file", tags = "Files")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File download was successful"),
            @ApiResponse(responseCode = "500", description = "File download failed")
    })
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

    @Operation(summary = "Download a specific version of a file", description = "User downloads a specific version of a file", tags = "Files")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File version download was successful"),
            @ApiResponse(responseCode = "500", description = "File version download failed")
    })
    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFileVersion(
            @AuthenticationPrincipal OAuth2User oauth2User,
            @PathVariable String fileName,
            @RequestParam(required = false) String versionId) {

        // Temporary userId for testing purposes
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

        try {
            // Generate the temporary file path for the download
            String tempFilePath = System.getProperty("java.io.tmpdir") + "/" + UUID.randomUUID() + "_" + fileName;

            // Call the file service to download the specific version
            File downloadedFile = fileService.downloadFileVersion(
                    UUID.fromString(userId), fileName, versionId, tempFilePath);

            // Convert the file to a Resource for HTTP response
            Path filePath = downloadedFile.toPath();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("Failed to download file version: " + fileName);
            }

            // Set up HTTP response headers for file download
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to download file version: " + fileName, e);
        }
    }



    @Operation(summary = "List files", description = "List all user files", tags = "Files")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of all user files")
    })
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



    @Operation(summary = "List object versions", description = "Retrieve a list of object versions", tags = "Files")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of object versions"),
            @ApiResponse(responseCode = "500", description = "Failed to retrieve object versions")
    })

    @GetMapping("/versions")
    public ResponseEntity<?> listObjectVersions(@AuthenticationPrincipal OAuth2User oauth2User) {
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
        try {
            return ResponseEntity.ok(fileService.getFileVersions(UUID.fromString(userId)));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to fetch object versions.");
        }
    }
}
