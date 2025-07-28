package in.lazygod.dto;

import in.lazygod.enums.FileRights;
import in.lazygod.enums.ResourceType;
import lombok.Data;

@Data
public class GrantRightsRequest {
    private String username;
    private String resourceId;
    private ResourceType resourceType;
    private FileRights rightsType;
}
