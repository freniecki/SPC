package spc.cloud.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.net.URI;
import java.net.URL;

@Service
public class S3Service {
    private final String bucketName;
    private final S3Client s3;

    public S3Service(@Value("${aws.s3.bucket}") String bucketName,
                     @Value("${aws.region}") String region,
                     @Value("${aws.s3.accessKey}") String accessKey,
                     @Value("${aws.s3.secretKey}") String secretKey,
                     @Value("${aws.s3.endpoint}") String endpoint) {
        this.bucketName = bucketName;
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)));
        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        this.s3 = builder.build();
    }

    /**
     * Uploading files to the system
     */
    public String uploadFile(String key, File file) {
        s3.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key).build(), file.toPath()
        );
        return key;
    }

    public URL generatePresignedUrl(String key) {
        return s3.utilities().getUrl(b -> b.bucket(bucketName).key(key));
    }

    /**
     * Download a file from S3
     */
    public File downloadFile(String key, String downloadPath) {
        File destinationFile = new File(downloadPath);
        s3.getObject(b -> b.bucket(bucketName).key(key), destinationFile.toPath());
        return destinationFile;
    }

    public void deleteFile(String key) {
        s3.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(key).build());
    }
}
