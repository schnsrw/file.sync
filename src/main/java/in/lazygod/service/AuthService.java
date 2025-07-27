package in.lazygod.service;

import in.lazygod.FileManagerApplication;
import in.lazygod.dto.AuthResponse;
import in.lazygod.dto.RegisterRequest;
import in.lazygod.dto.VerificationRequest;
import in.lazygod.enums.Role;
import in.lazygod.enums.Verification;
import in.lazygod.models.Folder;
import in.lazygod.models.User;
import in.lazygod.repositories.FolderRepository;
import in.lazygod.repositories.UserRepository;
import in.lazygod.security.JwtUtil;
import in.lazygod.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.Optional;

@Service @Slf4j @RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SnowflakeIdGenerator idGenerator;
    private final JwtUtil jwtUtil;
    private final FolderRepository folderRepository;


    public User register(@RequestBody RegisterRequest request) {
        Optional<User> existing = userRepository.findByUsername(request.getUsername());
        if (existing.isPresent()) {
            throw new in.lazygod.exception.BadRequestException("user.exists");
        }

        User user = User.builder()
                .userId(idGenerator.nextId())
                .username(request.getUsername())
                .fullName(request.getFullName())
                .email(request.getEmail()) // placeholder
                .verification(Verification.PENDING)
                // later will replace this random code generator when Emails are integrated
                .verificationCode(passwordEncoder.encode("121212"))
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .isActive(true)
                .build();

        return userRepository.save(user);
    }

    public AuthResponse generateTokens(User user){
        String token = jwtUtil.generateToken(user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        return new AuthResponse(token, refreshToken);
    }

    @Transactional
    public AuthResponse verifyUser(String userId, VerificationRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new in.lazygod.exception.NotFoundException("user.not.found"));

        if(passwordEncoder.matches(request.getVerificationCode(), user.getVerificationCode())){
            user.setVerification(Verification.VERIFIED);
            userRepository.save(user);

            Folder folder = Folder.builder()
                    .folderId(user.getUsername())
                    .isActive(true)
                    .storage(FileManagerApplication.DEFAULT_STORAGE)
                    .updatedOn(LocalDateTime.now())
                    .createdOn(LocalDateTime.now())
                    .build();
            folderRepository.save(folder);

            return generateTokens(user);
        }

        throw new in.lazygod.exception.BadRequestException("verification.failed");
    }
}
