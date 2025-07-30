package in.lazygod;

import in.lazygod.enums.*;
import in.lazygod.models.Folder;
import in.lazygod.models.Storage;
import in.lazygod.models.User;
import in.lazygod.models.UserRights;
import in.lazygod.repositories.FolderRepository;
import in.lazygod.repositories.StorageRepository;
import in.lazygod.repositories.UserRepository;
import in.lazygod.repositories.UserRightsRepository;
import in.lazygod.security.JwtUtil;
import in.lazygod.util.EncryptionUtil;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

@SpringBootApplication
@EnableScheduling
public class FileManagerApplication implements ApplicationRunner {

    public static Storage DEFAULT_STORAGE;

    public static void main(String[] args) {
        SpringApplication.run(FileManagerApplication.class, args);
    }

    private final UserRepository userRepository;
    private final StorageRepository storageRepository;
    private final PasswordEncoder passwordEncoder;
    private final FolderRepository folderRepository;
    private final UserRightsRepository rightsRepository;

    public FileManagerApplication(UserRepository userRepository,
                                  StorageRepository storageRepository,
                                  PasswordEncoder passwordEncoder,
                                  FolderRepository folderRepository,
                                  UserRightsRepository rightsRepository) {
        this.userRepository = userRepository;
        this.storageRepository = storageRepository;
        this.passwordEncoder = passwordEncoder;
        this.folderRepository = folderRepository;
        this.rightsRepository = rightsRepository;
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

    @Value("${jwt.secret}")
    private String jwtKey;

    @Value("${jwt.expiration.hr}")
    private long jwtExpirationHr;

    @Value("${encryption.secret}")
    private String encryptionKey;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        JwtUtil.SECRET_KEY = jwtKey;
        JwtUtil.EXPIRATION = jwtExpirationHr * 1000 * 60 * 60;
        JwtUtil.key = Keys.hmacShaKeyFor(JwtUtil.SECRET_KEY.getBytes());
        EncryptionUtil.setSecret(encryptionKey);

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

        Optional<Folder> baseFolder = folderRepository.findById(username);

        Folder folder = baseFolder.orElseGet(() -> {
            Folder folderBase = Folder.builder()
                    .folderId(admin.getUsername())
                    .isActive(true)
                    .storage(FileManagerApplication.DEFAULT_STORAGE)
                    .updatedOn(LocalDateTime.now())
                    .createdOn(LocalDateTime.now())
                    .build();
            folderBase = folderRepository.save(folderBase);

            rightsRepository.save(UserRights.builder()
                    .urId(folderBase.getFolderId())
                    .userId(admin.getUserId())
                    .fileId(folderBase.getFolderId())
                    .rightsType(FileRights.ADMIN)
                    .resourceType(ResourceType.FOLDER)
                    .isFavourite(false)
                    .isActive(true)
                    .createdOn(LocalDateTime.now())
                    .updatedOn(LocalDateTime.now())
                    .build());
            return folderBase;
        });


    }
}