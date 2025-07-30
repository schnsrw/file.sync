package in.lazygod.websocket.repositories;

import in.lazygod.websocket.model.RecentMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RecentMessageRepository extends MongoRepository<RecentMessage, String> {
    List<RecentMessage> findTop50ByConversationIdOrderByTimestampDesc(String conversationId);
}
