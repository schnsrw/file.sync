package in.lazygod.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/file")
@SecurityRequirement(name = "bearer-key")
public class FileController {

    @GetMapping("/secure-data")
    public ResponseEntity<String> securedEndpoint() {
        return ResponseEntity.ok("Only with JWT!");
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/only")
    public String adminOnly() {
        return "You are an admin!";
    }
}