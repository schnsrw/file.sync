package in.lazygod.websocket.service;

import in.lazygod.websocket.model.RecentMessage;
import in.lazygod.websocket.repositories.RecentMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class RecentMessageService {
    private final RecentMessageRepository repository;

    public void ingest(String conversationId, String from, String to, String text) {
        RecentMessage msg = RecentMessage.builder()
                .conversationId(conversationId)
                .from(from)
                .to(to)
                .content(text)
                .timestamp(Instant.now())
                .build();
        repository.save(msg);
    }

    public List<RecentMessage> recent(String userA, String userB, Instant before, int limit) {
        String conv = conversationId(userA, userB);
        Pageable pageable = PageRequest.of(0, limit);
        return repository.findByConversationIdAndTimestampBeforeOrderByTimestampDesc(conv, before, pageable);
    }

    private String conversationId(String a, String b) {
        if (a.compareTo(b) < 0) return a + ":" + b;
        return b + ":" + a;
    }
}
