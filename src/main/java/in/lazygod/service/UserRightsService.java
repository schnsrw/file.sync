package in.lazygod.service;

import in.lazygod.dto.GrantRightsRequest;
import in.lazygod.enums.FileRights;
import in.lazygod.enums.ResourceType;
import in.lazygod.models.User;
import in.lazygod.models.UserRights;
import in.lazygod.repositories.UserRepository;
import in.lazygod.repositories.UserRightsRepository;
import in.lazygod.security.SecurityContextHolderUtil;
import in.lazygod.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional
    public UserRights grantRights(GrantRightsRequest request) {
        User current = SecurityContextHolderUtil.getCurrentUser();

        // Only admins can grant rights
        if (!current.getRole().name().equals("ROLE_ADMIN")) {
            throw new in.lazygod.exception.ForbiddenException("not.authorized");
        }

        User target = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new in.lazygod.exception.NotFoundException("user.not.found"));

        UserRights rights = UserRights.builder()
                .urId(idGenerator.nextId())
                .userId(target.getUserId())
                .rightsType(request.getRightsType())
                .resourceType(request.getResourceType())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .isActive(true)
                .isFavourite(false)
                .build();

        if (request.getResourceType() == ResourceType.FILE) {
            rights.setFileId(request.getResourceId());
        } else {
            rights.setParentFolderId(request.getResourceId());
        }

        return rightsRepository.save(rights);
    }

    @Transactional
    public void revokeRights(String urId) {
        rightsRepository.deleteById(urId);
    }

    public List<UserRights> listRights(String resourceId, ResourceType type) {
        if (type == ResourceType.FILE) {
            return rightsRepository.findAllByFileIdAndResourceType(resourceId, ResourceType.FILE);
        }
        return rightsRepository.findAllByParentFolderIdAndResourceType(resourceId, ResourceType.FOLDER);
    }
}
