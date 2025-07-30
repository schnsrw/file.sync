package in.lazygod.service;

import in.lazygod.models.Storage;
import in.lazygod.models.User;
import in.lazygod.repositories.StorageRepository;
import in.lazygod.security.SecurityContextHolderUtil;
import in.lazygod.util.SnowflakeIdGenerator;
import in.lazygod.util.SimpleMultipartFile;
import in.lazygod.stoageUtils.StorageFactory;
import in.lazygod.stoageUtils.StorageImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Default implementation for {@link StorageManagementService}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StorageManagementServiceImpl implements StorageManagementService {

    private final StorageRepository storageRepository;
    private final SnowflakeIdGenerator idGenerator;

    @Override
    public Storage createStorage(Storage storage) {
        User owner = SecurityContextHolderUtil.getCurrentUser();
        log.info("Creating storage {} for user {}", storage.getStorageName(), owner.getUserId());
        storage.setStorageId(idGenerator.nextId());
        storage.setOwner(owner);
        storage.setCreatedOn(LocalDateTime.now());
        storage.setUpdatedOn(LocalDateTime.now());
        storage.setActive(true);
        return storageRepository.save(storage);
    }

    @Override
    public List<Storage> listStorages() {
        User owner = SecurityContextHolderUtil.getCurrentUser();
        log.debug("Listing storages for user {}", owner.getUserId());
        return storageRepository.findByOwner(owner);
    }

    @Override
    public boolean testCredentials(Storage storage) {
        try {
            StorageImpl impl = StorageFactory.getStorageImpl(storage);
            String path = storage.getBasePath() == null ? "" : storage.getBasePath();
            if (!path.isEmpty() && !path.endsWith("/")) {
                path += "/";
            }
            path += "credential-test-" + System.nanoTime();

            var file = new SimpleMultipartFile("test", "test.txt", "text/plain", new byte[0]);
            impl.upload(file, path);
            boolean exists = impl.exists(path);
            impl.delete(path);
            return exists;
        } catch (Exception e) {
            log.warn("Credential test failed", e);
            return false;
        }
    }
}
