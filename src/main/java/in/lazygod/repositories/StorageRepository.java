package in.lazygod.repositories;

import in.lazygod.models.Storage;
import in.lazygod.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StorageRepository extends JpaRepository<Storage, String> {
    List<Storage> findByOwner(User owner);
}
