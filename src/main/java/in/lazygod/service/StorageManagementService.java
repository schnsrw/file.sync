package in.lazygod.service;

import in.lazygod.models.Storage;
import in.lazygod.models.User;

import java.util.List;

/**
 * Service responsible for CRUD operations on {@link Storage} entities.
 */
public interface StorageManagementService {

    Storage createStorage(Storage storage, User owner);

    List<Storage> listStorages(User owner);
}
