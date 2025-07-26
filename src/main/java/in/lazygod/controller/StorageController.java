package in.lazygod.controller;

import in.lazygod.models.Storage;
import in.lazygod.models.User;
import in.lazygod.service.StorageManagementService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/storage")
@SecurityRequirement(name = "bearer-key")
@RequiredArgsConstructor
@Slf4j
public class StorageController {

    private final StorageManagementService storageManagementService;

    @PostMapping
    public ResponseEntity<Storage> createStorage(@RequestBody Storage storage) {
        // Owner is currently fetched via the provided owner_id field
        User owner = storage.getOwner();
        Storage saved = storageManagementService.createStorage(storage, owner);
        log.info("Created storage {}", saved.getStorageId());
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<java.util.List<Storage>> listStorages(@RequestBody User user) {
        var list = storageManagementService.listStorages(user);
        return ResponseEntity.ok(list);
    }
}
