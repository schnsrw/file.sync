package in.lazygod.dto;

import in.lazygod.enums.FileRights;
import in.lazygod.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RightsInfo {
    private String urId;
    private String userId;
    private String username;
    private String fullName;
    private String email;
    private String fileId;
    private String parentFolderId;
    private FileRights rightsType;
    private ResourceType resourceType;
}
