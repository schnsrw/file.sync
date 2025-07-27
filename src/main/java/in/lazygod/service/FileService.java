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

    private final StorageService storageService;
    private final FileRepository fileRepository;
    private final UserRightsRepository rightsRepository;
    private final ActivityLogRepository activityRepository;
    private final SnowflakeIdGenerator idGenerator;
    private final FolderRepository folderRepository;

    @Transactional
    public File upload(MultipartFile file, String folderId){

        User user = SecurityContextHolderUtil.getCurrentUser();

        Folder folder = folderId==null || folderId.isBlank() ?
                folderRepository.findById(user.getUsername()).orElseThrow(()-> new RuntimeException("Folder not found") )
                :folderRepository.findById(folderId).orElseThrow(()-> new RuntimeException("Folder not found") );

        UserRights folderRight = rightsRepository.findByUserIdAndFileIdAndResourceType(user.getUserId(), folderId, ResourceType.FOLDER)
                .orElseThrow(() -> new RuntimeException("Resource not authorized"));

        if (!folderRight.getRightsType().equals(FileRights.ADMIN)
                && !folderRight.getRightsType().equals(FileRights.WRITE)) {
            throw new RuntimeException("Action not authorized");
        }

        String fileId = idGenerator.nextId();
        String path = folder.getStorage().getBasePath() + "/" + fileId;

       // todo add default storage option and uplaod via it
 //        storageService.upload(file, path);

        File entity = File.builder()
                .fileId(fileId)
                .owner(user)
                .displayName(file.getOriginalFilename())
                .storage(folder.getStorage())
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

    private void setRightsForFile(User user, Folder folder, File file){

        List<UserRights> rights = rightsRepository.findAllByFileIdAndResourceType(folder.getFolderId(),ResourceType.FOLDER);

        List<UserRights> fileRights = rights.stream().map(right ->{
            return UserRights.builder()
                    .urId(idGenerator.nextId())
                    .userId(right.getUserId())
                    .fileId(file.getFileId())
                    .rightsType(
                            right.getUserId().equals(user.getUserId()) ?(right.getRightsType() == FileRights.ADMIN)
                                    ? FileRights.ADMIN :FileRights.WRITE :  right.getRightsType())
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

        File file = fileRepository.findById(fileId).orElseThrow(()-> new RuntimeException("Resource not found"));

        UserRights rights = rightsRepository.findByUserIdAndFileIdAndResourceType(user.getUserId(), file.getFileId(), ResourceType.FILE)
                .orElseThrow(()->new RuntimeException("Resource not authorized"));

        activityRepository.save(ActivityLog.builder()
                .activityId(idGenerator.nextId())
                .userId(user.getUserId())
                .action(ACTIONS.DOWNLOAD)
                .resourceType(ResourceType.FILE)
                .targetId(fileId)
                .timestamp(LocalDateTime.now())
                .build());

        return new FileResponse(file.getDisplayName(), storageService.download(file.getPath()));
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
                .action(fav ? ACTIONS.FAV: ACTIONS.UN_FAV)
                .resourceType(ResourceType.FILE)
                .targetId(fileId)
                .timestamp(LocalDateTime.now())
                .build());
    }
}
