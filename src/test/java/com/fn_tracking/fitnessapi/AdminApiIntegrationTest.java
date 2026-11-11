package com.fn_tracking.fitnessapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fn_tracking.fitnessapi.entity.Role;
import com.fn_tracking.fitnessapi.entity.User;
import com.fn_tracking.fitnessapi.repository.UserRepository;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private ChatLanguageModel chatLanguageModel;

    @BeforeEach
    void ensureAdminExists() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            userRepository.save(User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("secret123"))
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build());
        }
    }

    @Test
    void normalUserShouldBeForbiddenFromAdminEndpoints() throws Exception {
        register("admin_normal_user");
        String token = login("admin_normal_user");

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void adminCanListUsersChangeRoleAndReadStats() throws Exception {
        register("admin_target_user");
        String adminToken = login("admin");

        mockMvc.perform(get("/api/admin/users")
                        .param("keyword", "admin_target_user")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].username").value("admin_target_user"))
                .andExpect(jsonPath("$.content[0].role").value("USER"));

        Long targetId = userRepository.findByUsername("admin_target_user").orElseThrow().getId();

        mockMvc.perform(put("/api/admin/users/{id}/role", targetId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        mockMvc.perform(get("/api/admin/stats")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").isNumber())
                .andExpect(jsonPath("$.totalCheckIns").isNumber())
                .andExpect(jsonPath("$.totalDietPlans").isNumber())
                .andExpect(jsonPath("$.usersMissingCheckInToday").isNumber());
    }

    @Test
    void adminCanLockAndUnlockUser() throws Exception {
        register("admin_lock_target");
        String adminToken = login("admin");
        Long targetId = userRepository.findByUsername("admin_lock_target").orElseThrow().getId();

        mockMvc.perform(put("/api/admin/users/{id}/status", targetId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("enabled", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "admin_lock_target",
                                "password", "secret123"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Account is locked"));

        mockMvc.perform(put("/api/admin/users/{id}/status", targetId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("enabled", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));

        login("admin_lock_target");
    }

    private void register(String username) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "email", username + "@example.com",
                                "password", "secret123"
                        ))))
                .andExpect(status().isOk());
    }

    private String login(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "password", "secret123"
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token")
                .asText();
    }
}
