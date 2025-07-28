package in.lazygod.controller;

import in.lazygod.models.User;
import in.lazygod.dto.UserUpdateRequest;
import in.lazygod.security.SecurityContextHolderUtil;
import in.lazygod.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@SecurityRequirement(name = "bearer-key")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<User> me() {
        return ResponseEntity.ok(SecurityContextHolderUtil.getCurrentUser());
    }

    @GetMapping("/connected")
    public ResponseEntity<List<User>> request(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        return ResponseEntity.ok(userService.getConnections(PageRequest.of(page, size)));
    }

    @PostMapping("/{username}/disconnect")
    public ResponseEntity<String> disconnect(@PathVariable String username) {

        boolean success = userService.disconnect(username);

        return success ? ResponseEntity.accepted().build() : ResponseEntity.badRequest().build();
    }

    @PatchMapping("/me")
    public ResponseEntity<User> updateProfile(@RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }
}
