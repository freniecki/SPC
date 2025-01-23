package spc.cloud.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
        
        enableBucketVersioning(this.bucketName);
    }
    
    public void enableBucketVersioning(String bucketName) {
        try {
            s3.putBucketVersioning(PutBucketVersioningRequest.builder()
                    .bucket(bucketName)
                    .versioningConfiguration(VersioningConfiguration.builder()
                            .status(BucketVersioningStatus.ENABLED)
                            .build())
                    .build());
        } catch (Exception e) {
            System.err.println("Failed to enable versioning for bucket " + bucketName + ": " + e.getMessage());
        }
    }

    /**
     * Uploading files to the system
     */
    public String uploadFile(String key, InputStream inputStream, long contentLength, String fileType) {
        s3.putObject(PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(fileType)
                .build(), RequestBody.fromInputStream(inputStream, contentLength));
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

    // Downloading a specific version of a file from S3
    public File downloadFileVersion(String key, String versionId, String downloadPath) {
        // Download a specific version using the version ID
        File destinationFile = new File(downloadPath);
        s3.getObject(b -> b.bucket(bucketName).key(key).versionId(versionId), destinationFile.toPath());
        return destinationFile;
    }

    public void deleteFile(String key) {
        s3.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(key).build());
    }

    public List<String> listFiles(String prefix) {
        List<String> fileNames = new ArrayList<>();
        // get all objects by prefix(userid + '/')
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();
        ListObjectsV2Response response = s3.listObjectsV2(request);
        response.contents().forEach(object -> fileNames.add(object.key()));
        System.out.println("Found " + fileNames.size() + " files in bucket " + bucketName);
        return fileNames;
    }

    public ListObjectVersionsResponse listObjectVersions(String prefix) {
        return s3.listObjectVersions(b -> b.bucket(bucketName).prefix(prefix));
    }
}
