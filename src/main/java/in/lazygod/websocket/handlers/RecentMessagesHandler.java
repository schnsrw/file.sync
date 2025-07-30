package in.lazygod.websocket.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import in.lazygod.websocket.model.Packet;
import in.lazygod.websocket.model.RecentMessage;
import in.lazygod.websocket.model.SessionWrapper;
import in.lazygod.websocket.service.RecentMessageService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RecentMessagesHandler implements WsMessageHandler {
    private final RecentMessageService service;
    private final HandlerRegistry registry;
    private final ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        registry.register(this);
    }

    @Override
    public String type() {
        return "recent";
    }

    @Override
    public void handle(SessionWrapper wrapper, JsonNode payload) throws Exception {
        if (!payload.has("user")) return;
        String other = payload.get("user").asText();
        String me = wrapper.getUserWrapper().getUsername();
        List<RecentMessage> msgs = service.recent(me, other, 50);
        ArrayNode arr = mapper.valueToTree(msgs);
        wrapper.sendAsync(Packet.builder()
                .from("system")
                .to(me)
                .type("recent")
                .payload(arr)
                .build());
    }
}
