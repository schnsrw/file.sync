package in.lazygod.service;

import in.lazygod.dto.FileResponse;
import in.lazygod.enums.ACTIONS;
import in.lazygod.enums.FileRights;
import in.lazygod.enums.ResourceType;
import in.lazygod.models.*;
import in.lazygod.repositories.ActivityLogRepository;
import in.lazygod.repositories.FileRepository;
import in.lazygod.repositories.FolderRepository;
import in.lazygod.repositories.UserRightsRepository;
import in.lazygod.security.SecurityContextHolderUtil;
import in.lazygod.stoageUtils.StorageFactory;
import in.lazygod.stoageUtils.StorageImpl;
import in.lazygod.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final UserRightsRepository rightsRepository;
    private final ActivityLogRepository activityRepository;
    private final SnowflakeIdGenerator idGenerator;
    private final FolderRepository folderRepository;

    @Transactional
    public File upload(MultipartFile file, String folderId) throws IOException {

        User user = SecurityContextHolderUtil.getCurrentUser();

        Folder folder = folderId == null || folderId.isBlank() ?
                folderRepository.findById(user.getUsername()).orElseThrow(() -> new in.lazygod.exception.NotFoundException("folder.not.found"))
                : folderRepository.findById(folderId).orElseThrow(() -> new in.lazygod.exception.NotFoundException("folder.not.found"));

        UserRights folderRight = rightsRepository.findByUserIdAndFileIdAndResourceType(user.getUserId(), folder.getFolderId(), ResourceType.FOLDER)
                .orElseThrow(() -> new in.lazygod.exception.ForbiddenException("resource.not.authorized"));

        if (!folderRight.getRightsType().equals(FileRights.ADMIN)
                && !folderRight.getRightsType().equals(FileRights.WRITE)) {
            throw new in.lazygod.exception.ForbiddenException("action.not.authorized");
        }

        String fileId = idGenerator.nextId();
        String path = folder.getStorage().getBasePath() + "/" + fileId;

        StorageImpl storageImpl = StorageFactory.getStorageImpl(folder.getStorage());
        storageImpl.upload(file, path);

        File entity = File.builder()
                .fileId(fileId)
                .owner(user)
                .displayName(file.getOriginalFilename())
                .storage(folder.getStorage())
                .parentFolder(folder)
                .fileSize(file.getSize())
                .version("1")
                .path(path)
                .mimeType(file.getContentType())
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        entity = fileRepository.save(entity);

        setRightsForFile(user, folder, entity);

        activityRepository.save(ActivityLog.builder()
                .activityId(idGenerator.nextId())
                .userId(user.getUserId())
                .action(ACTIONS.UPLOAD)
                .resourceType(ResourceType.FILE)
                .targetId(fileId)
                .timestamp(LocalDateTime.now())
                .build());

        return entity;
    }

    private void setRightsForFile(User user, Folder folder, File file) {

        List<UserRights> rights = rightsRepository.findAllByFileIdAndResourceType(folder.getFolderId(), ResourceType.FOLDER);

        List<UserRights> fileRights = rights.stream().map(right -> {
            return UserRights.builder()
                    .urId(idGenerator.nextId())
                    .userId(right.getUserId())
                    .fileId(file.getFileId())
                    .parentFolderId(folder.getFolderId())
                    .rightsType(
                            right.getUserId().equals(user.getUserId()) ? (right.getRightsType() == FileRights.ADMIN)
                                    ? FileRights.ADMIN : FileRights.WRITE : right.getRightsType())
                    .resourceType(ResourceType.FILE)
                    .isFavourite(false)
                    .isActive(true)
                    .createdOn(LocalDateTime.now())
                    .updatedOn(LocalDateTime.now())
                    .build();
        }).toList();

        rightsRepository.saveAll(fileRights);
    }

    @Transactional
    public FileResponse download(String fileId) throws IOException {
        User user = SecurityContextHolderUtil.getCurrentUser();

        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new in.lazygod.exception.NotFoundException("resource.not.found"));

        UserRights rights = rightsRepository.findByUserIdAndFileIdAndResourceType(user.getUserId(), file.getFileId(), ResourceType.FILE)
                .orElseThrow(() -> new in.lazygod.exception.ForbiddenException("resource.not.authorized"));

        activityRepository.save(ActivityLog.builder()
                .activityId(idGenerator.nextId())
                .userId(user.getUserId())
                .action(ACTIONS.DOWNLOAD)
                .resourceType(ResourceType.FILE)
                .targetId(fileId)
                .timestamp(LocalDateTime.now())
                .build());

        StorageImpl storageImpl = StorageFactory.getStorageImpl(file.getStorage());
        return new FileResponse(file.getDisplayName(), storageImpl.download(file.getPath()));
    }

    @Transactional
    public void markFavorite(String fileId, boolean fav) {

        User user = SecurityContextHolderUtil.getCurrentUser();

        UserRights rights = rightsRepository.findByUserIdAndFileId(user.getUserId(), fileId)
                .orElseThrow();
        rights.setFavourite(fav);
        rights.setUpdatedOn(LocalDateTime.now());
        rightsRepository.save(rights);

        activityRepository.save(ActivityLog.builder()
                .activityId(idGenerator.nextId())
                .userId(user.getUserId())
                .action(fav ? ACTIONS.FAV : ACTIONS.UN_FAV)
                .resourceType(ResourceType.FILE)
                .targetId(fileId)
                .timestamp(LocalDateTime.now())
                .build());
    }
}
