package in.lazygod.repositories;

import in.lazygod.models.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

    List<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
}
