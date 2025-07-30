package in.lazygod.stoageUtils;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;

/**
 * AWS S3 based implementation for file storage. Supports both direct
 * server-side uploads/downloads and generation of presigned URLs for
 * clients to interact with S3 directly.
 */
public class S3Storage implements StorageImpl {

    private final S3Client s3Client;
    private final S3Presigner presigner;

    public S3Storage(String accessId, String accessKey) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessId, accessKey);
        this.s3Client = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.US_EAST_1)
                .serviceConfiguration(S3Configuration.builder()
                        .checksumValidationEnabled(false)
                        .build())
                .build();

        this.presigner = S3Presigner.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.US_EAST_1)
                .build();
    }

    private static String bucket(String path) {
        int idx = path.indexOf('/');
        return idx == -1 ? path : path.substring(0, idx);
    }

    private static String key(String path) {
        int idx = path.indexOf('/');
        return idx == -1 ? "" : path.substring(idx + 1);
    }

    @Override
    public void upload(MultipartFile file, String destinationPath) throws IOException {
        String bucket = bucket(destinationPath);
        String key = key(destinationPath);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
    }

    @Override
    public Resource download(String storagePath) throws IOException {
        String bucket = bucket(storagePath);
        String key = key(storagePath);

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        try (var in = s3Client.getObject(request)) {
            byte[] data = in.readAllBytes();
            return new ByteArrayResource(data);
        }
    }

    @Override
    public void delete(String storagePath) {
        String bucket = bucket(storagePath);
        String key = key(storagePath);

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        s3Client.deleteObject(request);
    }

    @Override
    public boolean exists(String storagePath) {
        String bucket = bucket(storagePath);
        String key = key(storagePath);

        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    @Override
    public String generatePresignedUploadUrl(String destinationPath, Duration expiry) {
        String bucket = bucket(destinationPath);
        String key = key(destinationPath);

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(objectRequest)
                .signatureDuration(expiry)
                .build();

        return presigner.presignPutObject(presignRequest).url().toString();
    }

    @Override
    public String generatePresignedDownloadUrl(String storagePath, Duration expiry) {
        String bucket = bucket(storagePath);
        String key = key(storagePath);

        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(objectRequest)
                .signatureDuration(expiry)
                .build();

        return presigner.presignGetObject(presignRequest).url().toString();
    }
}
