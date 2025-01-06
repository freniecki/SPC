package spc.cloud.service;


import org.springframework.stereotype.Service;
import spc.cloud.entity.User;
import spc.cloud.entity.UserFile;
import spc.cloud.repository.FileRepository;
import spc.cloud.repository.UserRepository;

import java.io.File;
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

    public UserFile uploadFile(UUID userId, File file, String fileType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String key = userId + "/" + file.getName();
        s3Service.uploadFile(key, file);

        UserFile uploadedFile = new UserFile();
        uploadedFile.setUser(user);
        uploadedFile.setFileName(file.getName());
        uploadedFile.setFileType(fileType);
        uploadedFile.setFileSize(file.length());
        uploadedFile.setS3Key(key);

        return fileRepository.save(uploadedFile);
    }

    /**
     * Batch upload multiple files for a user
     */
    public List<UserFile> uploadFiles(UUID userId, List<File> files, String fileType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return files.stream().map(file ->
                this.uploadFile(userId, file, fileType)
        ).collect(Collectors.toList());
    }

    /**
     * Download a single file from S3
     */
    public File downloadFile(UUID userId, String fileName, String downloadPath) {
        String key = userId + "/" + fileName;
        return s3Service.downloadFile(key, downloadPath);
    }
}
