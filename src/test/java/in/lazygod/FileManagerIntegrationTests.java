package in.lazygod;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.lazygod.models.File;
import in.lazygod.models.Folder;
import in.lazygod.models.Storage;
import in.lazygod.models.User;
import in.lazygod.repositories.FileRepository;
import in.lazygod.repositories.FolderRepository;
import in.lazygod.repositories.UserRepository;
import in.lazygod.repositories.UserRightsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class FileManagerIntegrationTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FileRepository fileRepository;

    @Autowired
    FolderRepository folderRepository;

    @Autowired
    UserRightsRepository rightsRepository;

    @Test
    void fullFlow() throws Exception {
        String username = "testuser" + System.currentTimeMillis();
        String password = "password";

        // Register user
        String registerJson = "{" +
                "\"username\":\"" + username + "\"," +
                "\"password\":\"" + password + "\"," +
                "\"fullName\":\"Test User\"," +
                "\"email\":\"test@example.com\"}";
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isOk());

        User user = userRepository.findByUsername(username).orElseThrow();

        // Login
        String loginJson = "{" +
                "\"username\":\"" + username + "\"," +
                "\"password\":\"" + password + "\"}";
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode loginNode = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = loginNode.get("accessToken").asText();

        // Create storage
        Storage storage = new Storage();
        storage.setStorageName("Test Storage");
        storage.setOwner(user);
        String storageJson = objectMapper.writeValueAsString(storage);
        MvcResult storageResult = mockMvc.perform(post("/storage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(storageJson))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode storageNode = objectMapper.readTree(storageResult.getResponse().getContentAsString());
        String storageId = storageNode.get("storageId").asText();

        // List storages
        String listJson = objectMapper.writeValueAsString(user);
        mockMvc.perform(get("/storage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(listJson))
                .andExpect(status().isOk());

        // Create root folder
        MvcResult rootResult = mockMvc.perform(post("/folder")
                        .param("userId", user.getUserId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode rootNode = objectMapper.readTree(rootResult.getResponse().getContentAsString());
        String rootFolderId = rootNode.get("folderId").asText();

        // Create sub folder
        MvcResult subResult = mockMvc.perform(post("/folder")
                        .param("userId", user.getUserId())
                        .param("parentId", rootFolderId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode subNode = objectMapper.readTree(subResult.getResponse().getContentAsString());
        String subFolderId = subNode.get("folderId").asText();

        // Mark folder favourite
        mockMvc.perform(post("/folder/" + rootFolderId + "/favourite")
                        .param("userId", user.getUserId())
                        .param("fav", "true")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        assertThat(rightsRepository.findByUserIdAndParentFolderId(user.getUserId(), rootFolderId))
                .get()
                .extracting("isFavourite")
                .isEqualTo(true);

        // Upload file
        byte[] content = "hello world".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE, content);
        MvcResult uploadResult = mockMvc.perform(multipart("/file/upload")
                        .file(file)
                        .param("userId", user.getUserId())
                        .param("storageId", storageId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode fileNode = objectMapper.readTree(uploadResult.getResponse().getContentAsString());
        String fileId = fileNode.get("fileId").asText();

        // Mark file favourite
        mockMvc.perform(post("/file/" + fileId + "/favourite")
                        .param("userId", user.getUserId())
                        .param("fav", "true")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        assertThat(rightsRepository.findByUserIdAndFileId(user.getUserId(), fileId))
                .get()
                .extracting("isFavourite")
                .isEqualTo(true);

        // Download file and verify content
        MvcResult downloadResult = mockMvc.perform(get("/file/" + fileId + "/download")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        byte[] downloaded = downloadResult.getResponse().getContentAsByteArray();
        assertThat(downloaded).isEqualTo(content);

        // Ensure file and folders exist in repositories
        assertThat(fileRepository.findById(fileId)).isPresent();
        assertThat(folderRepository.findById(rootFolderId)).isPresent();
        assertThat(folderRepository.findById(subFolderId)).isPresent();
    }
}
