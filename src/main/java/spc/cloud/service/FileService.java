package spc.cloud.service;


import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsResponse;
import spc.cloud.entity.User;
import spc.cloud.entity.UserFile;
import spc.cloud.repository.FileRepository;
import spc.cloud.repository.UserRepository;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileService {
    private final S3Service s3Service;
    private final LogService logService;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    public FileService(S3Service s3Service, LogService logService, FileRepository fileRepository, UserRepository userRepository) {
        this.s3Service = s3Service;
        this.logService = logService;
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
    }

    public UserFile uploadFile(UUID userId, InputStream inputStream, String fileName, String fileType, long fileSize) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String key = userId + "/" + fileName;

        // Check if the file already exists
        UserFile existingFile = fileRepository.findUserFileByS3Key(key);
        if (existingFile != null) {
            fileRepository.delete(existingFile);
        }

        s3Service.uploadFile(key, inputStream, fileSize, fileType);

        UserFile uploadedFile = new UserFile();
        uploadedFile.setUser(user);
        uploadedFile.setFileName(fileName);
        uploadedFile.setFileType(fileType);
        uploadedFile.setFileSize(fileSize);
        uploadedFile.setS3Key(key);
        uploadedFile.setPresignedUrl(s3Service.generatePresignedUrl(key));
        uploadedFile.setVersionId("");
        uploadedFile.setIsLatest(true);

        logService.putLogEvent("FILE UPLOAD (S3): User " + user.getUserId() + " File Key " + key);
        return fileRepository.save(uploadedFile);
    }

    /**
     * Generate presigned url for the file
     */
    public URL generatePresignedUrl(UUID userId, String fileName) {
        String key = userId + "/" + fileName;
        return this.s3Service.generatePresignedUrl(key);
    }
    /**
     * Download a single file from S3
     */
    public File downloadFile(UUID userId, String fileName, String downloadPath) {
        String key = userId + "/" + fileName;
        logService.putLogEvent("FILE DOWNLOAD (S3): User " + userId.toString() + " File Key " + key);
        return s3Service.downloadFile(key, downloadPath);
    }

    /**
     * Download a specific version of a file from S3.
     *
     * @param userId      the ID of the user.
     * @param fileName    the name of the file.
     * @param versionId   the specific version ID of the file.
     * @param downloadPath the local path to download the file.
     * @return the downloaded file.
     */

    public File downloadFileVersion(UUID userId, String fileName, String versionId, String downloadPath) {
        String key = userId + "/" + fileName;
        logService.putLogEvent("FILE VERSION DOWNLOAD (S3): User " + userId.toString() + " File Key " + key + " Version " + versionId);
        return s3Service.downloadFileVersion(key, versionId, downloadPath);
    }


    public List<String> listFileKeys(UUID userId) {
        String prefix = userId.toString() + "/";
        return s3Service.listFiles(prefix);
    }

    public List<UserFile> getUserFiles(UUID userId) {
        List<String> fileKeys = this.listFileKeys(userId);
        List<UserFile> userFiles = new ArrayList<>();
        fileKeys.forEach(key ->
                userFiles.add(this.fileRepository.findUserFileByS3Key(key))
        );
        return userFiles;
    }
    
    
    /**
     * Fetches all file versions for a user from S3. 
     * File versions are exclusively stored in S3 and should be downloaded from there.
     * In the database, only the latest version of a file is stored.
     * The UserFile objects returned by this method are not fully populated 
     * and are intended to be used within the file versioning system or page.
     *
     * @param userId the ID of the user whose file versions are to be fetched.
     * @return a list of UserFile objects representing the file versions in S3.
     */
    public List<UserFile> getFileVersions(UUID userId) {
        Optional<User> user = userRepository.findById(userId);

        try {
            ListObjectVersionsResponse versionsResponse = s3Service.listObjectVersions(userId.toString() + "/");
            if (versionsResponse.versions() == null || versionsResponse.versions().isEmpty()) {
                return new ArrayList<>();
            }
            return versionsResponse.versions().stream().map(v -> {
                UserFile userFile = new UserFile();
                userFile.setFileName(v.key());
                userFile.setFileType(null); // Set appropriate values if available
                userFile.setFileSize(0); // Set appropriate values if available
                userFile.setS3Key(v.key());
                userFile.setPresignedUrl(s3Service.generatePresignedUrl(v.key())); // Set appropriate values if available
                userFile.setVersionId(v.versionId());
                userFile.setIsLatest(v.isLatest());
                userFile.setUser(user.orElse(null)); // Set appropriate values if available
                userFile.setFileId(null);
                userFile.setFileSize(v.size() != null ? v.size() : 0);
                return userFile;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
