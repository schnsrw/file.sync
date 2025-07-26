package in.lazygod.repositories;

import in.lazygod.models.UserRights;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRightsRepository extends JpaRepository<UserRights, String> {
    java.util.Optional<UserRights> findByUserIdAndFileId(String userId, String fileId);
    java.util.Optional<UserRights> findByUserIdAndParentFolderId(String userId, String parentFolderId);
}
