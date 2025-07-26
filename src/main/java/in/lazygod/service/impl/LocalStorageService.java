package in.lazygod.service.impl;

import in.lazygod.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class LocalStorageService implements StorageService {

    /**
     * Base location on the local filesystem where files will be stored.
     */
    @Value("${storage.local.base-path}")
    private Path basePath;

    @Override
    public void upload(MultipartFile file, String destinationPath) throws IOException {
        Path target = basePath.resolve(destinationPath).normalize();
        Files.createDirectories(target.getParent());
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public Resource download(String storagePath) throws IOException {
        Path filePath = basePath.resolve(storagePath).normalize();
        return new UrlResource(filePath.toUri());
    }

    @Override
    public void delete(String storagePath) throws IOException {
        Files.deleteIfExists(basePath.resolve(storagePath).normalize());
    }

    @Override
    public boolean exists(String storagePath) {
        return Files.exists(basePath.resolve(storagePath).normalize());
    }
}
