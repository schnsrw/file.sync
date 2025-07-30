package in.lazygod.admin;

import in.lazygod.models.User;
import in.lazygod.repositories.UserRepository;
import in.lazygod.stoageUtils.StorageFactory;
import in.lazygod.websocket.manager.UserSessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("wsSessions", UserSessionManager.getInstance().getActiveSessions());
        model.addAttribute("metrics", AdminUtil.systemMetrics());
        return "admin/index";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable("id") String id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setActive(!user.isActive());
            userRepository.save(user);
        });
        return "redirect:/admin";
    }

    @PostMapping("/cache/cleanup")
    public String cleanupCache() {
        StorageFactory.cleanExpired();
        UserSessionManager.getInstance().cleanupInactiveSessions();
        return "redirect:/admin";
    }

    @PostMapping("/ws/{sessionId}/close")
    public String closeWs(@PathVariable String sessionId) {
        UserSessionManager.getInstance().closeById(sessionId);
        return "redirect:/admin";
    }

    @GetMapping("/logs")
    public String logs(Model model) throws IOException {
        model.addAttribute("logLines", AdminUtil.tailLog(Path.of("logs/application.log"), 200));
        return "admin/logs";
    }

    @GetMapping("/login")
    public String login() {
        return "admin/login";
    }
}
