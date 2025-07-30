package in.lazygod.controller;

import in.lazygod.models.Storage;
import in.lazygod.service.StorageManagementService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/storage")
@SecurityRequirement(name = "bearer-key")
@RequiredArgsConstructor
@Slf4j
public class StorageController {

    private final StorageManagementService storageManagementService;

    @PostMapping
    public ResponseEntity<Storage> createStorage(@RequestBody Storage storage) {
        Storage saved = storageManagementService.createStorage(storage);
        log.info("Created storage {}", saved.getStorageId());
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<java.util.List<Storage>> listStorages() {
        var list = storageManagementService.listStorages();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/test")
    public ResponseEntity<Void> test(@RequestBody Storage storage) {
        boolean ok = storageManagementService.testCredentials(storage);
        if (ok) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
}
