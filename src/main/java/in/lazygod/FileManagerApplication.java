package in.lazygod;

import in.lazygod.enums.Role;
import in.lazygod.enums.StorageType;
import in.lazygod.enums.Verification;
import in.lazygod.models.Storage;
import in.lazygod.models.User;
import in.lazygod.repositories.StorageRepository;
import in.lazygod.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

@SpringBootApplication
public class FileManagerApplication implements ApplicationRunner {

    public static Storage DEFAULT_STORAGE ;

    public static void main(String[] args) {
        SpringApplication.run(FileManagerApplication.class, args);
    }

    private final UserRepository userRepository;
    private final StorageRepository storageRepository;
    private final PasswordEncoder passwordEncoder;

    public FileManagerApplication(UserRepository userRepository,
                                  StorageRepository storageRepository,
                                  PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.storageRepository = storageRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Value("${storage.type}")
    private StorageType storageType;

    @Value("${storage.id}")
    private String storageId;

    @Value("${storage.local.base-path}")
    private String basePath;

    @Value("${storage.access_id}")
    private String accessId;

    @Value("${storage.access_key}")
    private String accessKey;

    @Value("${system.admin.username}")
    private String username;

    @Value("${system.admin.email}")
    private String email;

    @Value("${system.admin.password}")
    private String rawPassword;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Optional<User> existing = userRepository.findByUsername(username);

        User admin = existing.orElseGet(() -> {
            User newUser = User.builder()
                    .userId(username)
                    .username(username)
                    .email(email)
                    .fullName("System Admin")
                    .isActive(true)
                    .createdOn(LocalDateTime.now())
                    .updatedOn(LocalDateTime.now())
                    .password(passwordEncoder.encode(rawPassword))
                    .role(Role.ROLE_ADMIN)
                    .verification(Verification.VERIFIED)
                    .build();
            return userRepository.save(newUser);
        });

        boolean storageExists = storageRepository.existsById(storageId);
        if (!storageExists) {
            Storage storage = Storage.builder()
                    .storageId(storageId)
                    .storageName("Default Storage")
                    .basePath(basePath)
                    .storageType(storageType)
                    .owner(admin)
                    .isActive(true)
                    .createdOn(LocalDateTime.now())
                    .updatedOn(LocalDateTime.now())
                    .accessId(accessId)
                    .accessKey(accessKey)
                    .build();

            DEFAULT_STORAGE = storageRepository.save(storage);
        }
    }
}