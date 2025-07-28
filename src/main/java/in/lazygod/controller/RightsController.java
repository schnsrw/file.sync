package in.lazygod.controller;

import in.lazygod.dto.GrantRightsRequest;
import in.lazygod.enums.ResourceType;
import in.lazygod.models.UserRights;
import in.lazygod.service.UserRightsService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rights")
@SecurityRequirement(name = "bearer-key")
@RequiredArgsConstructor
public class RightsController {

    private final UserRightsService userRightsService;

    @PostMapping
    public ResponseEntity<UserRights> grant(@RequestBody GrantRightsRequest request) {
        return ResponseEntity.ok(userRightsService.grantRights(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revoke(@PathVariable("id") String id) {
        userRightsService.revokeRights(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<UserRights>> list(@RequestParam String resourceId,
                                                 @RequestParam ResourceType resourceType) {
        return ResponseEntity.ok(userRightsService.listRights(resourceId, resourceType));
    }
}
