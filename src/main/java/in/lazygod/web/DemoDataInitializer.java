package in.lazygod.web;

import in.lazygod.FileManagerApplication;
import in.lazygod.enums.*;
import in.lazygod.models.Connection;
import in.lazygod.models.Folder;
import in.lazygod.models.User;
import in.lazygod.models.UserRights;
import in.lazygod.repositories.ConnectionRepository;
import in.lazygod.repositories.FolderRepository;
import in.lazygod.repositories.UserRepository;
import in.lazygod.repositories.UserRightsRepository;
import in.lazygod.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DemoDataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final ConnectionRepository connectionRepository;
    private final PasswordEncoder passwordEncoder;
    private final SnowflakeIdGenerator idGenerator;
    private final FolderRepository folderRepository;
    private final UserRightsRepository rightsRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 1) {
            return; // admin already created
        }

        String[] names = {"alice", "bob", "carol", "dave", "eve"};
        List<User> created = new ArrayList<>();
        for (String n : names) {
            if (userRepository.findByUsername(n).isEmpty()) {
                User u = User.builder()
                        .userId(idGenerator.nextId())
                        .username(n)
                        .email(n + "@example.com")
                        .fullName(Character.toUpperCase(n.charAt(0)) + n.substring(1))
                        .password(passwordEncoder.encode("password"))
                        .role(Role.ROLE_USER)
                        .verification(Verification.VERIFIED)
                        .isActive(true)
                        .createdOn(LocalDateTime.now())
                        .updatedOn(LocalDateTime.now())
                        .build();
                created.add(userRepository.save(u));

                Optional<Folder> baseFolder = folderRepository.findById(n);

                Folder folder = baseFolder.orElseGet(() -> {
                    Folder folderBase = Folder.builder()
                            .folderId(n)
                            .isActive(true)
                            .storage(FileManagerApplication.DEFAULT_STORAGE)
                            .updatedOn(LocalDateTime.now())
                            .createdOn(LocalDateTime.now())
                            .build();
                    folderBase = folderRepository.save(folderBase);

                    rightsRepository.save(UserRights.builder()
                            .urId(folderBase.getFolderId())
                            .userId(u.getUserId())
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

        // create connections between all demo users
        for (int i = 0; i < created.size(); i++) {
            for (int j = i + 1; j < created.size(); j++) {
                User a = created.get(i);
                User b = created.get(j);
                Connection c = Connection.builder()
                        .connectionId(idGenerator.nextId())
                        .fromUserId(a.getUserId())
                        .toUserId(b.getUserId())
                        .status(ConnectionStatus.ACCEPTED)
                        .createdOn(LocalDateTime.now())
                        .updatedOn(LocalDateTime.now())
                        .build();
                connectionRepository.save(c);
            }
        }
    }
}
