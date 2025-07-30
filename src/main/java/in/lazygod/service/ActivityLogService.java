package in.lazygod.service;

import in.lazygod.enums.ResourceType;
import in.lazygod.models.ActivityLog;
import in.lazygod.repositories.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityRepository;

    public List<ActivityLog> listActivities(String resourceId, ResourceType resourceType,
                                            LocalDateTime before, int size) {
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        if (before == null) {
            return activityRepository
                    .findByResourceTypeAndTargetIdOrderByTimestampDesc(resourceType, resourceId, pageable);
        }
        return activityRepository
                .findByResourceTypeAndTargetIdAndTimestampLessThanOrderByTimestampDesc(
                        resourceType, resourceId, before, pageable);
    }
}
