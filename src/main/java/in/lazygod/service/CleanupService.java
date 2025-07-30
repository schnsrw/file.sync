package in.lazygod.service;

import in.lazygod.stoageUtils.StorageFactory;
import in.lazygod.websocket.manager.UserSessionManager;
import in.lazygod.repositories.FileRepository;
import in.lazygod.repositories.FolderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled maintenance tasks to keep the application tidy.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CleanupService {

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;

    private final FileService fileService;
    private final FolderService folderService;

    @Value("${trash.retention.days:30}")
    private long retentionDays;

    /**
     * Cleanup stale user sessions and expired cache entries.
     * The interval can be tuned via the 'cleanup.interval.ms' property.
     */
    @Scheduled(fixedDelayString = "${cleanup.interval.ms:600000}")
    public void runCleanup() {
        UserSessionManager.getInstance().cleanupInactiveSessions();
        StorageFactory.cleanExpired();
        purgeTrash();
        log.debug("cleanup completed");
    }

    private void purgeTrash() {
        var threshold = java.time.LocalDateTime.now().minusDays(retentionDays);

        var trashedFiles = fileRepository.findByTrashedIsTrueAndTrashedOnBefore(threshold);
        for (var f : trashedFiles) {
            try {
                fileService.deletePermanent(f.getFileId());
            } catch (Exception e) {
                log.error("Error deleting file {}", f.getFileId(), e);
            }
        }

        var trashedFolders = folderRepository.findByTrashedIsTrueAndTrashedOnBefore(threshold);
        for (var folder : trashedFolders) {
            try {
                folderService.deletePermanent(folder.getFolderId());
            } catch (Exception e) {
                log.error("Error deleting folder {}", folder.getFolderId(), e);
            }
        }
    }
}
