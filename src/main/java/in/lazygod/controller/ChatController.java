package in.lazygod.controller;

import in.lazygod.websocket.model.ChatMessage;
import in.lazygod.websocket.service.ChatService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@SecurityRequirement(name = "bearer-key")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/{username}")
    public ResponseEntity<List<ChatMessage>> fetchChat(@PathVariable String username,
            @RequestParam Long timestamp, @RequestParam Integer size){

        List<ChatMessage> messages = chatService.fetchChats(username, timestamp, size);
        return messages.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(messages);
    }
}
