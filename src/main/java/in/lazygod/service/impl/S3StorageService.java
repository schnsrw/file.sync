package in.lazygod.service.impl;

import in.lazygod.service.StorageService;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Placeholder implementation for S3 storage. The actual AWS S3 integration
 * can be provided by wiring the AWS SDK and injecting an AmazonS3 client.
 */
@Service
public class S3StorageService implements StorageService {

    @Override
    public void upload(MultipartFile file, String destinationPath) throws IOException {
        // TODO: integrate AWS SDK or other S3 compatible client
        throw new UnsupportedOperationException("S3 upload not implemented");
    }

    @Override
    public Resource download(String storagePath) throws IOException {
        throw new UnsupportedOperationException("S3 download not implemented");
    }

    @Override
    public void delete(String storagePath) throws IOException {
        throw new UnsupportedOperationException("S3 delete not implemented");
    }

    @Override
    public boolean exists(String storagePath) throws IOException {
        throw new UnsupportedOperationException("S3 exists not implemented");
    }
}
