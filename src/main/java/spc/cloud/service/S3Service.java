package spc.cloud.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.net.URL;

@Service
public class S3Service {
    private static final String BUCKET_NAME = "<bucketname-name>";
    private final S3Client s3;

    public S3Service() {
        this.s3 = S3Client.builder()
                .region(Region.EU_NORTH_1)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();
    }

    /**
     * Uploading files to the system
     */
    public String uploadFile(String key, File file) {
        s3.putObject(
                PutObjectRequest.builder()
                        .bucket(BUCKET_NAME)
                        .key(key).build(), file.toPath()
        );
        return key;
    }

    public URL generatePresignedUrl(String key) {
        return s3.utilities().getUrl(b -> b.bucket(BUCKET_NAME).key(key));
    }

    public void deleteFile(String key) {
        s3.deleteObject(DeleteObjectRequest.builder().bucket(BUCKET_NAME).key(key).build());
    }
}
