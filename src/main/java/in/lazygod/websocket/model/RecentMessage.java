package in.lazygod.websocket.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Lightweight message document for quick lookups.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "recent_messages")
public class RecentMessage {
    @Id
    private String id;
    private String conversationId;
    private String from;
    private String to;
    private String content;
    @Indexed(expireAfterSeconds = 604800) // 7 days
    private Instant timestamp;
}
