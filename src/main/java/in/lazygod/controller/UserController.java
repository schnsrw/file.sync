package in.lazygod.controller;

import in.lazygod.models.User;
import in.lazygod.repositories.UserRepository;
import in.lazygod.security.SecurityContextHolderUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@SecurityRequirement(name = "bearer-key")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<User> me() {
        return ResponseEntity.ok(SecurityContextHolderUtil.getCurrentUser());
    }
}
