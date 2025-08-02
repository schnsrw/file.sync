package in.lazygod.websocket.repositories;

import in.lazygod.websocket.model.ChatMessage;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByToAndDeliveredFalse(String to);

    List<ChatMessage> findByConversationIdAndTimestampBeforeOrderByTimestampDesc(
            String conversationId,
            Instant before,
            PageRequest pageable);
}
