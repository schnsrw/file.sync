package in.lazygod.controller;

import in.lazygod.enums.ResourceType;
import in.lazygod.models.ActivityLog;
import in.lazygod.service.ActivityLogService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/logs")
@SecurityRequirement(name = "bearer-key")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @GetMapping
    public ResponseEntity<List<ActivityLog>> list(
            @RequestParam String resourceId,
            @RequestParam ResourceType resourceType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime before,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                activityLogService.listActivities(resourceId, resourceType, before, size));
    }
}
