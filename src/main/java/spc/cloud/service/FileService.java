package spc.cloud.service;


import org.springframework.stereotype.Service;
import spc.cloud.entity.User;
import spc.cloud.entity.UserFile;
import spc.cloud.repository.FileRepository;
import spc.cloud.repository.UserRepository;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileService {
    private final S3Service s3Service;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    public FileService(S3Service s3Service, FileRepository fileRepository, UserRepository userRepository) {
        this.s3Service = s3Service;
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
    }

    public UserFile uploadFile(UUID userId, InputStream inputStream, String fileName, String fileType, long fileSize) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String key = userId + "/" + fileName;
        s3Service.uploadFile(key, inputStream, fileSize, fileType);

        UserFile uploadedFile = new UserFile();
        uploadedFile.setUser(user);
        uploadedFile.setFileName(fileName);
        uploadedFile.setFileType(fileType);
        uploadedFile.setFileSize(fileSize);
        uploadedFile.setS3Key(key);
        uploadedFile.setPresignedUrl(s3Service.generatePresignedUrl(key));

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
        return s3Service.downloadFile(key, downloadPath);
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
}
