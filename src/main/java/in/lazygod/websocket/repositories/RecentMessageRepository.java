package in.lazygod.websocket.repositories;

import in.lazygod.websocket.model.RecentMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface RecentMessageRepository extends MongoRepository<RecentMessage, String> {
    List<RecentMessage> findByConversationIdAndTimestampBeforeOrderByTimestampDesc(
            String conversationId,
            Instant before,
            Pageable pageable);
}
