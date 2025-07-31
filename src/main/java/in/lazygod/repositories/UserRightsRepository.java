package in.lazygod.repositories;

import in.lazygod.enums.ResourceType;
import in.lazygod.models.UserRights;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.Optional;

public interface UserRightsRepository extends JpaRepository<UserRights, String> {

    @Cacheable(value = "rights", key = "'uf:'+#userId+':'+#fileId")
    Optional<UserRights> findByUserIdAndFileId(String userId, String fileId);

    @Cacheable(value = "rights", key = "'up:'+#userId+':'+#parentFolderId")
    Optional<UserRights> findByUserIdAndParentFolderId(String userId, String parentFolderId);

    @Cacheable(value = "rights", key = "'ufr:'+#userId+':'+#fileId+':'+#resourceType")
    Optional<UserRights> findByUserIdAndFileIdAndResourceType(String userId, String fileId, ResourceType resourceType);

    @Cacheable(value = "rights", key = "'afr:'+#fileId+':'+#resourceType")
    List<UserRights> findAllByFileIdAndResourceType(String fileId, ResourceType resourceType);

    @Cacheable(value = "rights", key = "'apfr:'+#parentFolderId+':'+#resourceType")
    List<UserRights> findAllByParentFolderIdAndResourceType(String parentFolderId, ResourceType resourceType);

    @Cacheable(value = "rights", key = "'page:'+#userId+':'+#parentFolderId+':'+#resourceType+':'+#pageable.pageNumber+':'+#pageable.pageSize")
    Page<UserRights> findAllByUserIdAndParentFolderIdAndResourceType(
            String userId,
            String parentFolderId,
            ResourceType resourceType,
            Pageable pageable);
}
