package in.lazygod.cluster;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.lazygod.websocket.manager.UserSessionManager;
import in.lazygod.websocket.model.Packet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Duration;

/**
 * Handles Redis based cluster communication and user session registry.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClusterService implements MessageListener {

    private final RedisTemplate<String, Object> template;
    private final RedisMessageListenerContainer container;

    @Value("${cluster.enabled:false}")
    private boolean enabled;

    private final ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        if (enabled) {
            container.addMessageListener(this, new PatternTopic("user.*"));
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    /** Register user as online in Redis with TTL. */
    public void registerUser(String username) {
        if (!enabled) return;
        try {
            template.opsForValue().set("user:" + username, "1", Duration.ofSeconds(300));
        } catch (Exception e) {
            log.error("Failed to register user in cluster", e);
        }
    }

    /** Remove user session from Redis. */
    public void removeUser(String username) {
        if (!enabled) return;
        try {
            template.delete("user:" + username);
        } catch (Exception e) {
            log.error("Failed to remove user from cluster", e);
        }
    }

    /** Check if user is registered in Redis. */
    public boolean userExists(String username) {
        if (!enabled) return false;
        try {
            return Boolean.TRUE.equals(template.hasKey("user:" + username));
        } catch (Exception e) {
            return false;
        }
    }

    /** Publish a Packet to the user's channel. */
    public void publish(String username, Packet packet) {
        if (!enabled) return;
        try {
            String json = mapper.writeValueAsString(packet);
            template.convertAndSend("user." + username, json);
        } catch (Exception e) {
            log.error("Failed to publish packet", e);
        }
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        if (!enabled) return;
        String channel = new String(message.getChannel());
        if (!channel.startsWith("user.")) return;
        String username = channel.substring(5);
        if (!UserSessionManager.getInstance().isOnline(username)) return;
        try {
            String json = new String(message.getBody());
            Packet packet = mapper.readValue(json, Packet.class);
            UserSessionManager.getInstance().sendToUser(username, packet);
        } catch (Exception e) {
            log.error("Failed to handle cluster message", e);
        }
    }
}
