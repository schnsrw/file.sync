package in.lazygod.repositories;

import in.lazygod.enums.ConnectionStatus;
import in.lazygod.models.Connection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConnectionRepository extends JpaRepository<Connection, String> {
    Optional<Connection> findByFromUserIdAndToUserId(String fromUserId, String toUserId);
    List<Connection> findByToUserIdAndStatus(String toUserId, ConnectionStatus status);
    List<Connection> findByFromUserIdAndStatus(String fromUserId, ConnectionStatus status);
}
