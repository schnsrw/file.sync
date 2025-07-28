package in.lazygod.repositories;

import in.lazygod.enums.ResourceType;
import in.lazygod.models.UserRights;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRightsRepository extends JpaRepository<UserRights, String> {

    Optional<UserRights> findByUserIdAndFileId(String userId, String fileId);

    Optional<UserRights> findByUserIdAndParentFolderId(String userId, String parentFolderId);

    Optional<UserRights> findByUserIdAndFileIdAndResourceType(String userId, String fileId, ResourceType resourceType);

    List<UserRights> findAllByFileIdAndResourceType(String fileId, ResourceType resourceType);

    List<UserRights> findAllByParentFolderIdAndResourceType(String parentFolderId, ResourceType resourceType);

    Page<UserRights> findAllByUserIdAndParentFolderIdAndResourceType(
            String userId,
            String parentFolderId,
            ResourceType resourceType,
            Pageable pageable);
}
