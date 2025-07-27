package in.lazygod.controller;

import in.lazygod.models.Connection;
import in.lazygod.service.ConnectionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/connections")
@SecurityRequirement(name = "bearer-key")
@RequiredArgsConstructor
public class ConnectionController {

    private final ConnectionService connectionService;

    @PostMapping("/request/{username}")
    public ResponseEntity<Connection> request(@PathVariable String username) {
        return ResponseEntity.ok(connectionService.sendRequest(username));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<Connection> accept(@PathVariable("id") String id) {
        return ResponseEntity.ok(connectionService.acceptRequest(id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Connection> reject(@PathVariable("id") String id) {
        return ResponseEntity.ok(connectionService.rejectRequest(id));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Connection>> pending() {
        return ResponseEntity.ok(connectionService.pendingRequests());
    }
}
