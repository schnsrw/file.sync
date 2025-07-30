package in.lazygod.stoageUtils;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * General contract for file storage operations. Implementations may store
 * files on local disk, S3 or any remote server.
 */
public interface StorageImpl {

    /**
     * Upload a file to a specific location.
     *
     * @param file            the file to store
     * @param destinationPath relative destination path inside the storage
     */
    void upload(MultipartFile file, String destinationPath) throws IOException;

    /**
     * Retrieve a file from storage.
     *
     * @param storagePath relative path to the file in storage
     * @return the file as a {@link Resource}
     */
    Resource download(String storagePath) throws IOException;

    /**
     * Delete a file from storage.
     *
     * @param storagePath relative path to the file in storage
     */
    void delete(String storagePath) throws IOException;

    /**
     * Check if a given file exists in storage.
     *
     * @param storagePath relative path to the file in storage
     * @return true if the file exists
     */
    boolean exists(String storagePath) throws IOException;

    /**
     * Generate a presigned URL for uploading a file directly to the storage
     * backend. Implementations that do not support presigned URLs may throw
     * {@link UnsupportedOperationException}.
     *
     * @param destinationPath relative path in the storage
     * @param expiry          validity duration of the URL
     * @return URL that clients can use to upload
     */
    default String generatePresignedUploadUrl(String destinationPath, java.time.Duration expiry) {
        throw new UnsupportedOperationException("Presigned upload not supported");
    }

    /**
     * Generate a presigned URL for downloading a file directly from the storage
     * backend. Implementations that do not support presigned URLs may throw
     * {@link UnsupportedOperationException}.
     *
     * @param storagePath relative path to the file in storage
     * @param expiry      validity duration of the URL
     * @return URL that clients can use to download
     */
    default String generatePresignedDownloadUrl(String storagePath, java.time.Duration expiry) {
        throw new UnsupportedOperationException("Presigned download not supported");
    }
}
