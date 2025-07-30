package in.lazygod.service;

import in.lazygod.stoageUtils.StorageFactory;
import in.lazygod.websocket.manager.UserSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled maintenance tasks to keep the application tidy.
 */
@Component
@Slf4j
public class CleanupService {

    /**
     * Cleanup stale user sessions and expired cache entries.
     * The interval can be tuned via the 'cleanup.interval.ms' property.
     */
    @Scheduled(fixedDelayString = "${cleanup.interval.ms:600000}")
    public void runCleanup() {
        UserSessionManager.getInstance().cleanupInactiveSessions();
        StorageFactory.cleanExpired();
        log.debug("cleanup completed");
    }
}
