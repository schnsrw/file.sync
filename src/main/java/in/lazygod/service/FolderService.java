package in.lazygod.service;


import in.lazygod.dto.FolderContent;
import in.lazygod.enums.ACTIONS;
import in.lazygod.enums.FileRights;
import in.lazygod.enums.ResourceType;
import in.lazygod.models.ActivityLog;
import in.lazygod.models.Folder;
import in.lazygod.models.User;
import in.lazygod.models.UserRights;
import in.lazygod.repositories.ActivityLogRepository;
import in.lazygod.repositories.FileRepository;
import in.lazygod.repositories.FolderRepository;
import in.lazygod.repositories.UserRightsRepository;
import in.lazygod.security.SecurityContextHolderUtil;
import in.lazygod.util.SnowflakeIdGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;
    private final UserRightsRepository rightsRepository;
    private final ActivityLogRepository activityRepository;
    private final SnowflakeIdGenerator idGenerator;
    private final FileService fileService;

    @Transactional
    public Folder createFolder(String parentId, String folderName) {

        User user = SecurityContextHolderUtil.getCurrentUser();

        Folder parentFolder = parentId == null || parentId.isBlank() ?
                folderRepository.findById(user.getUsername()).orElseThrow(() -> new in.lazygod.exception.NotFoundException("folder.not.found"))
                : folderRepository.findById(parentId).orElseThrow(() -> new in.lazygod.exception.NotFoundException("folder.not.found"));

        UserRights folderRight = rightsRepository.findByUserIdAndFileIdAndResourceType(user.getUserId(), parentFolder.getFolderId(), ResourceType.FOLDER)
                .orElseThrow(() -> new in.lazygod.exception.ForbiddenException("resource.not.authorized"));

        if (!folderRight.getRightsType().equals(FileRights.ADMIN)
                && !folderRight.getRightsType().equals(FileRights.WRITE)) {
            throw new in.lazygod.exception.ForbiddenException("action.not.authorized");
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
                .orElseThrow(() -> new in.lazygod.exception.ForbiddenException("not.authorized"));

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

    @Transactional
    public FolderContent listContents(String folderId, int page, int size) {
        User user = SecurityContextHolderUtil.getCurrentUser();

        String targetId = (folderId == null || folderId.isBlank()) ? user.getUsername() : folderId;

        Folder folder = folderRepository.findById(targetId)
                .orElseThrow(() -> new in.lazygod.exception.NotFoundException("folder.not.found"));

        rightsRepository.findByUserIdAndParentFolderId(user.getUserId(), folder.getFolderId())
                .orElseThrow(() -> new in.lazygod.exception.ForbiddenException("resource.not.authorized"));

        Pageable pageable = PageRequest.of(page, size);

        // Fetch sub-folders visible to user
        var allSubFolders = folderRepository.findByParentFolder(folder);
        var accessibleFolders = allSubFolders.stream()
                .filter(f -> rightsRepository.findByUserIdAndParentFolderId(user.getUserId(), f.getFolderId()).isPresent())
                .skip((long) page * size)
                .limit(size)
                .toList();

        // Fetch files via rights table
        var fileRights = rightsRepository
                .findAllByUserIdAndParentFolderIdAndResourceType(user.getUserId(), folder.getFolderId(), ResourceType.FILE, pageable);
        var fileIds = fileRights.getContent().stream().map(UserRights::getFileId).toList();
        var files = fileRepository.findAllById(fileIds);

        activityRepository.save(ActivityLog.builder()
                .activityId(idGenerator.nextId())
                .userId(user.getUserId())
                .action(ACTIONS.READ)
                .resourceType(ResourceType.FOLDER)
                .targetId(folder.getFolderId())
                .timestamp(LocalDateTime.now())
                .build());

        return new FolderContent(accessibleFolders, files);
    }

    @Transactional
    public void moveToTrash(String folderId) {
        User user = SecurityContextHolderUtil.getCurrentUser();

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new in.lazygod.exception.NotFoundException("folder.not.found"));

        UserRights rights = rightsRepository.findByUserIdAndParentFolderId(user.getUserId(), folderId)
                .orElseThrow(() -> new in.lazygod.exception.ForbiddenException("resource.not.authorized"));

        if (rights.getRightsType() != FileRights.ADMIN) {
            throw new in.lazygod.exception.ForbiddenException("action.not.authorized");
        }

        moveFolderRecursive(folder, true);

        activityRepository.save(ActivityLog.builder()
                .activityId(idGenerator.nextId())
                .userId(user.getUserId())
                .action(ACTIONS.TRASH)
                .resourceType(ResourceType.FOLDER)
                .targetId(folderId)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @Transactional
    public void restore(String folderId) {
        User user = SecurityContextHolderUtil.getCurrentUser();

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new in.lazygod.exception.NotFoundException("folder.not.found"));

        UserRights rights = rightsRepository.findByUserIdAndParentFolderId(user.getUserId(), folderId)
                .orElseThrow(() -> new in.lazygod.exception.ForbiddenException("resource.not.authorized"));

        if (rights.getRightsType() != FileRights.ADMIN) {
            throw new in.lazygod.exception.ForbiddenException("action.not.authorized");
        }

        moveFolderRecursive(folder, false);

        activityRepository.save(ActivityLog.builder()
                .activityId(idGenerator.nextId())
                .userId(user.getUserId())
                .action(ACTIONS.RESTORE)
                .resourceType(ResourceType.FOLDER)
                .targetId(folderId)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @Transactional
    public void deletePermanent(String folderId) throws java.io.IOException {
        User user = SecurityContextHolderUtil.getCurrentUser();

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new in.lazygod.exception.NotFoundException("folder.not.found"));

        UserRights rights = rightsRepository.findByUserIdAndParentFolderId(user.getUserId(), folderId)
                .orElseThrow(() -> new in.lazygod.exception.ForbiddenException("resource.not.authorized"));

        if (rights.getRightsType() != FileRights.ADMIN) {
            throw new in.lazygod.exception.ForbiddenException("action.not.authorized");
        }

        deleteFolderRecursive(folder);

        activityRepository.save(ActivityLog.builder()
                .activityId(idGenerator.nextId())
                .userId(user.getUserId())
                .action(ACTIONS.DELETE)
                .resourceType(ResourceType.FOLDER)
                .targetId(folderId)
                .timestamp(LocalDateTime.now())
                .build());
    }

    private void moveFolderRecursive(Folder folder, boolean toTrash) {
        folder.setTrashed(toTrash);
        folder.setTrashedOn(toTrash ? LocalDateTime.now() : null);
        folderRepository.save(folder);

        var rights = rightsRepository.findAllByParentFolderIdAndResourceType(folder.getFolderId(), ResourceType.FOLDER);
        for (var r : rights) {
            r.setActive(!toTrash);
            r.setUpdatedOn(LocalDateTime.now());
        }
        rightsRepository.saveAll(rights);

        var files = fileRepository.findByParentFolder(folder);
        for (var f : files) {
            if (toTrash) {
                f.setTrashed(true);
                f.setTrashedOn(LocalDateTime.now());
            } else {
                f.setTrashed(false);
                f.setTrashedOn(null);
            }
            fileRepository.save(f);
            var fr = rightsRepository.findAllByFileIdAndResourceType(f.getFileId(), ResourceType.FILE);
            for (var r : fr) {
                r.setActive(!toTrash);
                r.setUpdatedOn(LocalDateTime.now());
            }
            rightsRepository.saveAll(fr);
        }

        var children = folderRepository.findByParentFolder(folder);
        for (var child : children) {
            moveFolderRecursive(child, toTrash);
        }
    }

    private void deleteFolderRecursive(Folder folder) throws java.io.IOException {
        var files = fileRepository.findByParentFolder(folder);
        for (var f : files) {
            fileService.deletePermanent(f.getFileId());
        }

        var children = folderRepository.findByParentFolder(folder);
        for (var child : children) {
            deleteFolderRecursive(child);
        }

        folder.setActive(false);
        folderRepository.save(folder);

        var rights = rightsRepository.findAllByParentFolderIdAndResourceType(folder.getFolderId(), ResourceType.FOLDER);
        for (var r : rights) {
            r.setActive(false);
            r.setUpdatedOn(LocalDateTime.now());
        }
        rightsRepository.saveAll(rights);
    }
}
