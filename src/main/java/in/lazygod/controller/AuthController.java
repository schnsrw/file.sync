package in.lazygod.controller;

import in.lazygod.dto.*;
import in.lazygod.models.User;
import in.lazygod.repositories.UserRepository;
import in.lazygod.security.JwtUtil;
import in.lazygod.security.SecurityContextHolderUtil;
import in.lazygod.service.AuthService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        return ResponseEntity.ok(
                authService.generateTokens(
                        SecurityContextHolderUtil.getCurrentUser()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        String username = jwtUtil.extractUsername(request.refreshToken);
        if (!jwtUtil.validateToken(request.refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new in.lazygod.exception.NotFoundException("user.not.found"));
        return ResponseEntity.ok(authService.generateTokens(user));
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest request) {

        User user = authService.register(request);
        return ResponseEntity.ok(user);
    }

    @PostMapping("{userId}/verify")
    public ResponseEntity<AuthResponse> verify(@RequestParam String userId,@RequestBody VerificationRequest request) {

        return ResponseEntity.ok(authService.verifyUser(userId, request));
    }

}
