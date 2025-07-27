package in.lazygod.service;

import in.lazygod.models.Storage;
import in.lazygod.models.User;
import in.lazygod.repositories.StorageRepository;
import in.lazygod.security.SecurityContextHolderUtil;
import in.lazygod.util.SnowflakeIdGenerator;
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
}
