package in.lazygod.service;

import in.lazygod.dto.GrantRightsRequest;
import in.lazygod.dto.RightsInfo;
import in.lazygod.enums.ACTIONS;
import in.lazygod.enums.FileRights;
import in.lazygod.enums.ResourceType;
import in.lazygod.models.User;
import in.lazygod.models.UserRights;
import in.lazygod.models.File;
import in.lazygod.models.Folder;
import in.lazygod.models.ActivityLog;
import in.lazygod.repositories.FileRepository;
import in.lazygod.repositories.FolderRepository;
import in.lazygod.repositories.UserRepository;
import in.lazygod.repositories.UserRightsRepository;
import in.lazygod.repositories.ActivityLogRepository;
import in.lazygod.security.SecurityContextHolderUtil;
import in.lazygod.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRightsService {

    private final UserRightsRepository rightsRepository;
    private final UserRepository userRepository;
    private final SnowflakeIdGenerator idGenerator;
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final ActivityLogRepository activityRepository;

    @Transactional
    @CacheEvict(value = "rights", allEntries = true)
    public UserRights grantRights(GrantRightsRequest request) {
        User current = SecurityContextHolderUtil.getCurrentUser();

        if (!current.getRole().name().equals("ROLE_ADMIN")) {
            throw new in.lazygod.exception.ForbiddenException("not.authorized");
        }

        User target = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new in.lazygod.exception.NotFoundException("user.not.found"));

        UserRights result;
        if (request.getResourceType() == ResourceType.FILE) {
            File file = fileRepository.findById(request.getResourceId())
                    .orElseThrow(() -> new in.lazygod.exception.NotFoundException("resource.not.found"));
            String parent = file.getParentFolder() == null ? null : file.getParentFolder().getFolderId();
            String parentForUser = determineParentFolder(target, parent);
            result = applyFileRights(target, file, parentForUser, request.getRightsType());
        } else {
            Folder folder = folderRepository.findById(request.getResourceId())
                    .orElseThrow(() -> new in.lazygod.exception.NotFoundException("resource.not.found"));
            String parent = folder.getParentFolder() == null ? null : folder.getParentFolder().getFolderId();
            String parentForUser = determineParentFolder(target, parent);
            applyFolderRightsRecursive(target, folder, parentForUser, request.getRightsType());
            result = rightsRepository.findByUserIdAndParentFolderId(target.getUserId(), folder.getFolderId())
                    .orElseThrow();
        }

        activityRepository.save(ActivityLog.builder()
                .activityId(idGenerator.nextId())
                .userId(current.getUserId())
                .action(ACTIONS.MODIFY)
                .resourceType(request.getResourceType())
                .targetId(request.getResourceId())
                .timestamp(LocalDateTime.now())
                .build());

        return result;
    }

    @Transactional
    @CacheEvict(value = "rights", allEntries = true)
    public void revokeRights(String resourceId, ResourceType type) {
        User current = SecurityContextHolderUtil.getCurrentUser();
        if (type == ResourceType.FILE) {
            rightsRepository.findByUserIdAndFileIdAndResourceType(current.getUserId(), resourceId, ResourceType.FILE)
                    .ifPresent(rightsRepository::delete);
        } else {
            rightsRepository.findByUserIdAndParentFolderId(current.getUserId(), resourceId)
                    .ifPresent(rightsRepository::delete);
        }

        activityRepository.save(ActivityLog.builder()
                .activityId(idGenerator.nextId())
                .userId(current.getUserId())
                .action(ACTIONS.MODIFY)
                .resourceType(type)
                .targetId(resourceId)
                .timestamp(LocalDateTime.now())
                .build());
    }

    public List<RightsInfo> listRights(String resourceId, ResourceType type) {
        List<UserRights> rights;
        if (type == ResourceType.FILE) {
            rights = rightsRepository.findAllByFileIdAndResourceType(resourceId, ResourceType.FILE);
        } else {
            rights = rightsRepository.findAllByParentFolderIdAndResourceType(resourceId, ResourceType.FOLDER);
        }

        return rights.stream().map(r -> {
            User u = userRepository.findById(r.getUserId()).orElse(null);
            return new RightsInfo(
                    r.getUrId(),
                    r.getUserId(),
                    u != null ? u.getUsername() : null,
                    u != null ? u.getFullName() : null,
                    u != null ? u.getEmail() : null,
                    r.getFileId(),
                    r.getParentFolderId(),
                    r.getRightsType(),
                    r.getResourceType()
            );
        }).toList();
    }

    private String determineParentFolder(User target, String parentFolderId) {
        if (parentFolderId == null) {
            return target.getUsername();
        }
        return rightsRepository.findByUserIdAndParentFolderId(target.getUserId(), parentFolderId)
                .or(() -> rightsRepository.findByUserIdAndFileIdAndResourceType(target.getUserId(), parentFolderId, ResourceType.FOLDER))
                .isPresent() ? parentFolderId : target.getUsername();
    }

    private FileRights higherRights(FileRights r1, FileRights r2) {
        if (r1 == FileRights.ADMIN || r2 == FileRights.ADMIN) return FileRights.ADMIN;
        if (r1 == FileRights.WRITE || r2 == FileRights.WRITE) return FileRights.WRITE;
        return FileRights.READ;
    }

    private UserRights applyFileRights(User target, File file, String parentFolderId, FileRights rightsType) {
        var existing = rightsRepository.findByUserIdAndFileIdAndResourceType(target.getUserId(), file.getFileId(), ResourceType.FILE);
        if (existing.isPresent()) {
            UserRights r = existing.get();
            r.setParentFolderId(parentFolderId);
            r.setRightsType(higherRights(r.getRightsType(), rightsType));
            r.setUpdatedOn(LocalDateTime.now());
            return rightsRepository.save(r);
        }

        UserRights rights = UserRights.builder()
                .urId(idGenerator.nextId())
                .userId(target.getUserId())
                .fileId(file.getFileId())
                .parentFolderId(parentFolderId)
                .rightsType(rightsType)
                .resourceType(ResourceType.FILE)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .isActive(true)
                .isFavourite(false)
                .build();
        return rightsRepository.save(rights);
    }

    private void applyFolderRightsRecursive(User target, Folder folder, String parentFolderId, FileRights rightsType) {
        var existing = rightsRepository.findByUserIdAndParentFolderId(target.getUserId(), folder.getFolderId());
        UserRights currentRights;
        if (existing.isPresent()) {
            currentRights = existing.get();
            currentRights.setRightsType(higherRights(currentRights.getRightsType(), rightsType));
            currentRights.setUpdatedOn(LocalDateTime.now());
        } else {
            currentRights = UserRights.builder()
                    .urId(idGenerator.nextId())
                    .userId(target.getUserId())
                    .fileId(folder.getFolderId())
                    .parentFolderId(folder.getFolderId())
                    .rightsType(rightsType)
                    .resourceType(ResourceType.FOLDER)
                    .createdOn(LocalDateTime.now())
                    .updatedOn(LocalDateTime.now())
                    .isActive(true)
                    .isFavourite(false)
                    .build();
        }
        rightsRepository.save(currentRights);

        // apply for sub files
        List<File> files = fileRepository.findByParentFolder(folder);
        for (File f : files) {
            applyFileRights(target, f, folder.getFolderId(), rightsType);
        }

        // recurse for subfolders
        List<Folder> children = folderRepository.findByParentFolder(folder);
        for (Folder child : children) {
            applyFolderRightsRecursive(target, child, folder.getFolderId(), rightsType);
        }
    }
}
