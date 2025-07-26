package in.lazygod.controller;

import in.lazygod.models.Storage;
import in.lazygod.repositories.StorageRepository;
import in.lazygod.util.SnowflakeIdGenerator;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/storage")
@SecurityRequirement(name = "bearer-key")
@RequiredArgsConstructor
public class StorageController {

    private final StorageRepository storageRepository;
    private final SnowflakeIdGenerator idGenerator;

    @PostMapping
    public ResponseEntity<Storage> createStorage(@RequestBody Storage storage) {
        storage.setStorageId(idGenerator.nextId());
        storage.setCreatedOn(LocalDateTime.now());
        storage.setUpdatedOn(LocalDateTime.now());
        storage.setActive(true);
        Storage saved = storageRepository.save(storage);
        return ResponseEntity.ok(saved);
    }
}
