package in.lazygod.service.impl;

import in.lazygod.service.StorageService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory storage service used for development and testing.
 */
import org.springframework.context.annotation.Primary;

@Service
@Primary
public class DummyStorageService implements StorageService {

    private final Map<String, byte[]> store = new ConcurrentHashMap<>();

    @Override
    public void upload(MultipartFile file, String destinationPath) throws IOException {
        store.put(destinationPath, file.getBytes());
    }

    @Override
    public Resource download(String storagePath) throws IOException {
        byte[] data = store.getOrDefault(storagePath, new byte[0]);
        return new ByteArrayResource(data);
    }

    @Override
    public void delete(String storagePath) throws IOException {
        store.remove(storagePath);
    }

    @Override
    public boolean exists(String storagePath) throws IOException {
        return store.containsKey(storagePath);
    }
}
