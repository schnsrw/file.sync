package in.lazygod.repositories;

import in.lazygod.enums.ResourceType;
import in.lazygod.models.ActivityLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, String> {

    List<ActivityLog> findByResourceTypeAndTargetIdOrderByTimestampDesc(ResourceType resourceType,
                                                                       String targetId,
                                                                       Pageable pageable);

    List<ActivityLog> findByResourceTypeAndTargetIdAndTimestampLessThanOrderByTimestampDesc(
            ResourceType resourceType,
            String targetId,
            LocalDateTime timestamp,
            Pageable pageable);
}
