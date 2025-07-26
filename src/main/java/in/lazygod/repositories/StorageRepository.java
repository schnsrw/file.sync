package in.lazygod.repositories;

import in.lazygod.models.Storage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageRepository extends JpaRepository<Storage, String> {
}
