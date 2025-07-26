package in.lazygod.controller.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UIController {

    @GetMapping({"/", "/ui"})
    public String index() {
        return "index";
    }

    @GetMapping("/ui/login")
    public String login() {
        return "login";
    }

    @GetMapping("/ui/register")
    public String register() {
        return "register";
    }

    @GetMapping("/ui/upload")
    public String upload() {
        return "upload";
    }

    @GetMapping("/ui/folder")
    public String folder() {
        return "folder";
    }

    @GetMapping("/ui/storage")
    public String storage() {
        return "storage";
    }
}
