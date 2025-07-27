package in.lazygod.controller;

import in.lazygod.dto.FileResponse;
import in.lazygod.models.File;
import in.lazygod.service.FileService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;

@RestController
@RequestMapping("/file")
@SecurityRequirement(name = "bearer-key")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<File> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String folderId) throws IOException {

        return ResponseEntity.ok(fileService.upload(file, folderId));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<StreamingResponseBody> download(@PathVariable("id") String fileId) throws IOException {

        FileResponse resource = fileService.download(fileId);
        StreamingResponseBody body = outputStream -> {
            try (var in = resource.getResource().getInputStream()) {
                in.transferTo(outputStream);
            }
        };
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + resource.getDisplayName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(body);
    }

    @PostMapping("/{id}/favourite")
    public ResponseEntity<Void> favourite(@PathVariable("id") String fileId,
                                          @RequestParam boolean fav) {

        fileService.markFavorite(fileId, fav);
        return ResponseEntity.ok().build();
    }
}
