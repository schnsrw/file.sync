package in.lazygod.repositories;

import in.lazygod.models.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, String> {
}
