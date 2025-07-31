package in.lazygod.service;

import in.lazygod.enums.ConnectionStatus;
import in.lazygod.exception.NotFoundException;
import in.lazygod.models.Connection;
import in.lazygod.models.User;
import in.lazygod.repositories.ConnectionRepository;
import in.lazygod.repositories.UserRepository;
import in.lazygod.security.SecurityContextHolderUtil;
import in.lazygod.dto.UserUpdateRequest;
import in.lazygod.websocket.handlers.NotificationHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final ConnectionRepository connectionRepository;
    private final UserRepository userRepository;

    public List<User> getConnections(PageRequest of) {

        User current = SecurityContextHolderUtil.getCurrentUser();
        List<Connection> connections = connectionRepository.findByToUserIdOrFromUserIdAndStatus(current.getUserId(), ConnectionStatus.ACCEPTED, of);

        if (connections.isEmpty()) {
            throw new NotFoundException("no.connection.found");
        }

        Set<String> userIds = new HashSet<>();

        connections.forEach(c -> userIds.add(c.getToUserId()));
        connections.forEach(c -> userIds.add(c.getFromUserId()));
        userIds.remove(current.getUserId());

        return userRepository.findAllById(userIds);
    }

    public boolean disconnect(String username) {
        User current = SecurityContextHolderUtil.getCurrentUser();
        User receipent = userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("user.not.found"));


        Connection connection = connectionRepository.findConnectionFromUserIds(current.getUserId(), receipent.getUserId(), ConnectionStatus.ACCEPTED)
                .orElseThrow(() -> new NotFoundException("connection.not.found"));

        connection.setStatus(ConnectionStatus.REJECTED);
        connection.setUpdatedOn(LocalDateTime.now());
        connectionRepository.save(connection);

        // notify requester if online
        NotificationHandler.send(receipent.getUsername(), current.getUsername(), "connection-rejected", null);

        return true;
    }

    @CacheEvict(value = "user-details", key = "T(in.lazygod.security.SecurityContextHolderUtil).getCurrentUser().getUsername()")
    public User updateProfile(UserUpdateRequest request) {
        User current = SecurityContextHolderUtil.getCurrentUser();
        if (request.getEmail() != null) {
            current.setEmail(request.getEmail());
        }
        if (request.getFullName() != null) {
            current.setFullName(request.getFullName());
        }
        current.setUpdatedOn(LocalDateTime.now());
        return userRepository.save(current);
    }
}
