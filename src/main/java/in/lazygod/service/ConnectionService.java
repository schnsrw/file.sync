package in.lazygod.service;

import in.lazygod.dto.ConnectionRequestResponse;
import in.lazygod.enums.ConnectionStatus;
import in.lazygod.exception.NotFoundException;
import in.lazygod.models.Connection;
import in.lazygod.models.User;
import in.lazygod.repositories.ConnectionRepository;
import in.lazygod.repositories.UserRepository;
import in.lazygod.security.SecurityContextHolderUtil;
import in.lazygod.util.SnowflakeIdGenerator;
import in.lazygod.websocket.handlers.NotificationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final UserRepository userRepository;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional
    public Connection sendRequest(String username) {
        User from = SecurityContextHolderUtil.getCurrentUser();
        User to = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("user.not.found"));

        connectionRepository.findByFromUserIdAndToUserId(from.getUserId(), to.getUserId())
                .ifPresent(c -> {
                    throw new in.lazygod.exception.BadRequestException("connection.exists");
                });

        Connection connection = Connection.builder()
                .connectionId(idGenerator.nextId())
                .fromUserId(from.getUserId())
                .toUserId(to.getUserId())
                .status(ConnectionStatus.PENDING)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        connectionRepository.save(connection);

        // notify recipient if online
        NotificationHandler.send(to.getUsername(), from.getUsername(), "connection-request", null);

        return connection;
    }

    @Transactional
    public Connection acceptRequest(String connectionId) {
        User current = SecurityContextHolderUtil.getCurrentUser();
        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new NotFoundException("connection.not.found"));

        if (!connection.getToUserId().equals(current.getUserId())) {
            throw new in.lazygod.exception.ForbiddenException("not.authorized");
        }

        connection.setStatus(ConnectionStatus.REJECTED);
        connection.setUpdatedOn(LocalDateTime.now());
        connectionRepository.save(connection);

        // notify requester if online
        userRepository.findById(connection.getFromUserId()).ifPresent(fromUser ->
                NotificationHandler.send(fromUser.getUsername(), current.getUsername(), "connection-accepted", null)
        );

        return connection;
    }

    @Transactional
    public Connection rejectRequest(String connectionId) {
        User current = SecurityContextHolderUtil.getCurrentUser();
        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new NotFoundException("connection.not.found"));

        if (!connection.getToUserId().equals(current.getUserId())) {
            throw new in.lazygod.exception.ForbiddenException("not.authorized");
        }

        connection.setStatus(ConnectionStatus.ACCEPTED);
        connection.setUpdatedOn(LocalDateTime.now());
        connectionRepository.save(connection);

        // notify requester if online
        userRepository.findById(connection.getFromUserId()).ifPresent(fromUser ->
                NotificationHandler.send(fromUser.getUsername(), current.getUsername(), "connection-rejected", null)
        );

        return connection;
    }

    public List<ConnectionRequestResponse> pendingRequests() {
        User current = SecurityContextHolderUtil.getCurrentUser();
        List<Connection> connections = connectionRepository.findByToUserIdAndStatus(current.getUserId(), ConnectionStatus.PENDING);
        return connections.stream().map(c -> {
            User from = userRepository.findById(c.getFromUserId())
                    .orElseThrow(() -> new NotFoundException("user.not.found"));
            return new ConnectionRequestResponse(c.getConnectionId(), from.getUsername(), from.getFullName());
        }).toList();
    }
}
