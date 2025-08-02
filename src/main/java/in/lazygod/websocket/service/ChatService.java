package in.lazygod.websocket.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.lazygod.enums.ConnectionStatus;
import in.lazygod.models.User;
import in.lazygod.repositories.ConnectionRepository;
import in.lazygod.repositories.UserRepository;
import in.lazygod.security.SecurityContextHolderUtil;
import in.lazygod.websocket.manager.UserSessionManager;
import in.lazygod.websocket.model.ChatMessage;
import in.lazygod.websocket.model.Packet;
import in.lazygod.websocket.repositories.ChatMessageRepository;
import in.lazygod.cluster.ClusterService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatMessageRepository repository;
    private final RecentMessageService recentService;
    private final UserRepository userRepository;
    private final ClusterService clusterService;
    private final ConnectionRepository connectionRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public void sendMessage(String from, String to, String text) {
        ChatMessage message = ChatMessage.builder()
                .conversationId(conversationId(from, to))
                .from(from)
                .to(to)
                .content(text)
                .timestamp(Instant.now())
                .delivered(false)
                .build();
        repository.save(message);
        recentService.ingest(message.getConversationId(), from, to, text);
        if (UserSessionManager.getInstance().isOnline(to)) {
            deliverMessage(message);
        } else if (clusterService.isEnabled() && clusterService.userExists(to)) {
            deliverRemote(message);
        }
    }

    public void deliverPending(String username) {
        List<ChatMessage> messages = repository.findByToAndDeliveredFalse(username);
        for (ChatMessage m : messages) {
            deliverMessage(m);
        }
    }

    private void deliverMessage(ChatMessage message) {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("id", message.getId());
        payload.put("text", message.getContent());

        UserSessionManager.getInstance().sendToUser(message.getTo(),
                Packet.builder()
                        .from(message.getFrom())
                        .to(message.getTo())
                        .type("chat")
                        .payload(payload)
                        .build());

        message.setDelivered(true);
        repository.save(message);

        ObjectNode receipt = mapper.createObjectNode();
        receipt.put("id", message.getId());
        receipt.put("status", "delivered");
        UserSessionManager.getInstance().sendToUser(message.getFrom(),
                Packet.builder()
                        .from("system")
                        .to(message.getFrom())
                        .type("receipt")
                        .payload(receipt)
                        .build());
    }

    private void deliverRemote(ChatMessage message) {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("id", message.getId());
        payload.put("text", message.getContent());

        clusterService.publish(message.getTo(),
                Packet.builder()
                        .from(message.getFrom())
                        .to(message.getTo())
                        .type("chat")
                        .payload(payload)
                        .build());

        message.setDelivered(true);
        repository.save(message);

        ObjectNode receipt = mapper.createObjectNode();
        receipt.put("id", message.getId());
        receipt.put("status", "delivered");
        UserSessionManager.getInstance().sendToUser(message.getFrom(),
                Packet.builder()
                        .from("system")
                        .to(message.getFrom())
                        .type("receipt")
                        .payload(receipt)
                        .build());
    }

    private String conversationId(String a, String b) {
        String[] arr = {a, b};
        Arrays.sort(arr);
        return arr[0] + ":" + arr[1];
    }

    public List<ChatMessage> fetchChats(String username, Long timestamp, Integer size) {
        User reciptiant = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("user.not.found"));

        User user = SecurityContextHolderUtil.getCurrentUser();

        // todo: check if user is connected
        connectionRepository.findConnectionFromUserIds(reciptiant.getUsername(), user.getUsername(), ConnectionStatus.ACCEPTED)
                .orElseThrow(()->new RuntimeException("connection.not.found"));

        String conversationId = conversationId(reciptiant.getUsername(),user.getUsername());

        return repository.findByConvsationIdAndBeforeTimestamp(conversationId,Instant.ofEpochMilli(timestamp),
                PageRequest.of(0,size, Sort.by(Sort.Direction.DESC,"timestamp")));
    }
}
