package in.lazygod.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.io.Resource;

@Data
@AllArgsConstructor
public class FileResponse {
    String displayName;
    Resource resource;
}
