package in.lazygod.websocket.repositories;

import in.lazygod.websocket.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByToAndDeliveredFalse(String to);
}
