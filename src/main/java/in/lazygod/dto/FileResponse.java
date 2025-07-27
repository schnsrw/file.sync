package in.lazygod.dto;

import org.springframework.core.io.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class FileResponse {
    String displayName;
    Resource resource;
}
