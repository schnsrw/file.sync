package in.lazygod.stoageUtils;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory storage service used for development and testing.
 */

public class DummyStorage implements StorageImpl {

    private final Map<String, byte[]> store = new ConcurrentHashMap<>();

    @Override
    public void upload(MultipartFile file, String destinationPath) throws IOException {
        try (var in = file.getInputStream();
             var out = new java.io.ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            store.put(destinationPath, out.toByteArray());
        }
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
