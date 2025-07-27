package in.lazygod.stoageUtils;

import in.lazygod.models.Storage;

public class StorageFactory {

    public static StorageImpl getStorageImpl(Storage config){

        switch (config.getStorageType()){
            case LOCAL -> {
                return new LocalStorage(config.getBasePath());
            }
            case S3 -> {
                return new S3Storage();
            }
            default -> {
                return new DummyStorage();
            }
        }
    }
}
