package in.lazygod.repositories;

import in.lazygod.models.Storage;
import in.lazygod.models.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageRepository extends JpaRepository<Storage, String> {
    List<Storage> findByOwner(User owner);
}
