package in.lazygod.service;

import in.lazygod.models.Storage;

import java.util.List;

/**
 * Service responsible for CRUD operations on {@link Storage} entities.
 */
public interface StorageManagementService {

    Storage createStorage(Storage storage);

    List<Storage> listStorages();

    boolean testCredentials(Storage storage);
}
