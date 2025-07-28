package in.lazygod.repositories;

import in.lazygod.enums.ConnectionStatus;
import in.lazygod.models.Connection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ConnectionRepository extends JpaRepository<Connection, String> {
    Optional<Connection> findByFromUserIdAndToUserId(String fromUserId, String toUserId);

    List<Connection> findByToUserIdAndStatus(String toUserId, ConnectionStatus status);

    @Query("SELECT C FROM Connection C where (toUserId = :toUserId OR fromUserId = :fromUserId ) AND status = :status")
    List<Connection> findByToUserIdOrFromUserIdAndStatus(String toUserId, ConnectionStatus status, PageRequest of);

    @Query("SELECT C FROM Connection C where ((toUserId = :toUserId AND fromUserId = :fromUserId ) OR (toUserId = :fromUserId AND fromUserId = :toUserId) ) AND status = :status")
    Optional<Connection> findConnectionFromUserIds(String toUserId, String fromUserId, ConnectionStatus status);


    List<Connection> findByFromUserIdAndStatus(String fromUserId, ConnectionStatus status);
}
