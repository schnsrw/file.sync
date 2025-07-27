package in.lazygod.service;


import in.lazygod.enums.ACTIONS;
import in.lazygod.enums.FileRights;
import in.lazygod.enums.ResourceType;
import in.lazygod.models.ActivityLog;
import in.lazygod.models.Folder;
import in.lazygod.models.User;
import in.lazygod.models.UserRights;
import in.lazygod.repositories.ActivityLogRepository;
import in.lazygod.repositories.FolderRepository;
import in.lazygod.repositories.UserRightsRepository;
import in.lazygod.security.SecurityContextHolderUtil;
import in.lazygod.util.SnowflakeIdGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final UserRightsRepository rightsRepository;
    private final ActivityLogRepository activityRepository;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional
    public Folder createFolder(String parentId,String folderName) {

        User user = SecurityContextHolderUtil.getCurrentUser();

        Folder parentFolder = parentId==null || parentId.isBlank() ?
                folderRepository.findById(user.getUsername()).orElseThrow(()-> new RuntimeException("Folder not found") )
                :folderRepository.findById(parentId).orElseThrow(()-> new RuntimeException("Folder not found") );

        UserRights folderRight = rightsRepository.findByUserIdAndFileIdAndResourceType(user.getUserId(), parentFolder.getFolderId(), ResourceType.FOLDER)
                .orElseThrow(() -> new RuntimeException("Resource not authorized"));

        if (!folderRight.getRightsType().equals(FileRights.ADMIN)
                && !folderRight.getRightsType().equals(FileRights.WRITE)) {
            throw new RuntimeException("Action not authorized");
        }
        Folder folder = Folder.builder()
                .folderId(idGenerator.nextId())
                .parentFolder(parentFolder)
                .storage(parentFolder.getStorage())
                .displayName(folderName)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        folderRepository.save(folder);

        // Get inherited rights from parent
        List<UserRights> parentRights = parentId == null
                ? List.of()
                : rightsRepository.findAllByFileIdAndResourceType(parentId, ResourceType.FOLDER);

        // Propagate rights
        List<UserRights> newRights = parentRights.isEmpty()
                ? List.of(UserRights.builder()
                .urId(idGenerator.nextId())
                .userId(user.getUserId())
                .parentFolderId(folder.getFolderId())
                .rightsType(FileRights.ADMIN)
                .resourceType(ResourceType.FOLDER)
                .isFavourite(false)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build())
                : parentRights.stream().map(r -> UserRights.builder()
                .urId(idGenerator.nextId())
                .userId(r.getUserId())
                .parentFolderId(folder.getFolderId())
                .rightsType(
                        r.getUserId().equals(user.getUserId()) && r.getRightsType() == FileRights.ADMIN
                                ? FileRights.ADMIN
                                : FileRights.WRITE
                )
                .resourceType(ResourceType.FOLDER)
                .isFavourite(false)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build()).toList();

        rightsRepository.saveAll(newRights);

        activityRepository.save(ActivityLog.builder()
                .activityId(idGenerator.nextId())
                .userId(user.getUserId())
                .action(ACTIONS.CREATE)
                .resourceType(ResourceType.FOLDER)
                .targetId(folder.getFolderId())
                .timestamp(LocalDateTime.now())
                .build());

        return folder;
    }

    @Transactional
    public void markFavourite(String folderId, boolean fav) {
        User user = SecurityContextHolderUtil.getCurrentUser();
        UserRights rights = rightsRepository.findByUserIdAndParentFolderId(user.getUserId(), folderId)
                .orElseThrow(() -> new RuntimeException("Not authorized"));

        rights.setFavourite(fav);
        rights.setUpdatedOn(LocalDateTime.now());
        rightsRepository.save(rights);

        activityRepository.save(ActivityLog.builder()
                .activityId(idGenerator.nextId())
                .userId(user.getUserId())
                .action(fav ? ACTIONS.FAV : ACTIONS.UN_FAV)
                .resourceType(ResourceType.FOLDER)
                .targetId(folderId)
                .timestamp(LocalDateTime.now())
                .build());
    }
}
