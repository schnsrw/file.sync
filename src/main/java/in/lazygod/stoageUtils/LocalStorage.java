package in.lazygod.stoageUtils;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;


public class LocalStorage implements StorageImpl {

    /**
     * Base location on the local filesystem where files will be stored.
     */
    private final Path basePath;

    public LocalStorage(String basePath) {
        this.basePath = new File(basePath).toPath();
    }

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
