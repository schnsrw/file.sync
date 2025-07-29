package in.lazygod.websocket.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import in.lazygod.websocket.model.SessionWrapper;
import in.lazygod.websocket.service.ChatService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatHandler implements WsMessageHandler {
    private final ChatService chatService;
    private final HandlerRegistry registry;

    @PostConstruct
    public void init() {
        registry.register(this);
    }

    @Override
    public String type() {
        return "chat";
    }

    @Override
    public void handle(SessionWrapper wrapper, JsonNode payload) {
        String to = payload.has("to") ? payload.get("to").asText() : null;
        String text = payload.has("text") ? payload.get("text").asText() : null;
        if (to == null || text == null) return;
        String from = wrapper.getUserWrapper().getUsername();
        chatService.sendMessage(from, to, text);
    }
}
