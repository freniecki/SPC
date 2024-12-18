package spc.cloud.service;


import org.springframework.stereotype.Service;
import spc.cloud.entity.User;
import spc.cloud.entity.UserFile;
import spc.cloud.repository.FileRepository;
import spc.cloud.repository.UserRepository;

import java.io.File;
import java.util.UUID;

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
}
