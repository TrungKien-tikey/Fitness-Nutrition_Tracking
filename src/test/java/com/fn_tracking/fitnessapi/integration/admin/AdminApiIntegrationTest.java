package com.fn_tracking.fitnessapi.integration.admin;

import com.fn_tracking.fitnessapi.entity.User;
import com.fn_tracking.fitnessapi.integration.support.AbstractIntegrationTest;
import com.fn_tracking.fitnessapi.integration.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminApiIntegrationTest extends AbstractIntegrationTest {

    @Test
    void normalUserShouldBeForbiddenFromAllAdminEndpoints() throws Exception {
        String token = registerAndLogin("admin_normal_user");

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        mockMvc.perform(get("/api/admin/stats")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/admin/users/{id}/role", 1L)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", "ADMIN"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/admin/users/{id}/status", 1L)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("enabled", false))))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpointsShouldRejectMissingOrInvalidToken() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanListUsersChangeRoleAndReadStats() throws Exception {
        createAdmin("admin");
        register("admin_target_user");
        String adminToken = login("admin");
        Long targetId = userRepository.findByUsername("admin_target_user").orElseThrow().getId();

        mockMvc.perform(get("/api/admin/users")
                        .param("keyword", "admin_target_user")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].username").value("admin_target_user"))
                .andExpect(jsonPath("$.content[0].role").value("USER"))
                .andExpect(jsonPath("$.content[0].password").doesNotExist());

        mockMvc.perform(put("/api/admin/users/{id}/role", targetId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        String promotedToken = login("admin_target_user");
        mockMvc.perform(get("/api/admin/stats")
                        .header("Authorization", "Bearer " + promotedToken))
                .andExpect(status().isOk());

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
        createAdmin("admin");
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
                        .content(objectMapper.writeValueAsString(
                                TestDataFactory.loginBody("admin_lock_target"))))
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

    @Test
    void lockedUserOldTokenShouldBeRejected() throws Exception {
        createAdmin("admin");
        String userToken = registerAndLogin("admin_locked_token_user");
        String adminToken = login("admin");
        Long targetId = userRepository.findByUsername("admin_locked_token_user").orElseThrow().getId();

        mockMvc.perform(put("/api/admin/users/{id}/status", targetId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("enabled", false))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/daily-logs/history")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldNotChangeOwnRoleOrStatus() throws Exception {
        User admin = createAdmin("admin");
        String adminToken = login("admin");

        mockMvc.perform(put("/api/admin/users/{id}/role", admin.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", "USER"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You cannot change your own role"));

        mockMvc.perform(put("/api/admin/users/{id}/status", admin.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("enabled", false))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You cannot change your own account status"));
    }

    @Test
    @WithMockUser(username = "external-admin", roles = "ADMIN")
    void lastAdminShouldNotBeDemotedOrLocked() throws Exception {
        User lastAdmin = createAdmin("last_admin");

        mockMvc.perform(put("/api/admin/users/{id}/role", lastAdmin.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", "USER"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot remove the last admin"));

        mockMvc.perform(put("/api/admin/users/{id}/status", lastAdmin.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("enabled", false))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot lock the last admin"));
    }

    @Test
    void adminShouldValidateRoleAndStatusBodies() throws Exception {
        createAdmin("admin");
        register("admin_validation_target");
        String adminToken = login("admin");
        Long targetId = userRepository.findByUsername("admin_validation_target").orElseThrow().getId();

        mockMvc.perform(put("/api/admin/users/{id}/role", targetId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"SUPER_ADMIN\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/admin/users/{id}/role", targetId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/admin/users/{id}/status", targetId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminShouldReturnBadRequestForUnknownUser() throws Exception {
        createAdmin("admin");
        String adminToken = login("admin");

        mockMvc.perform(put("/api/admin/users/{id}/role", 999999L)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", "ADMIN"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not found"));

        mockMvc.perform(put("/api/admin/users/{id}/status", 999999L)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("enabled", false))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void statsShouldReflectSeededData() throws Exception {
        createAdmin("admin");
        String token = registerAndLogin("admin_stats_user");

        mockMvc.perform(post("/api/daily-logs/check-in")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "caloriesIn", 2000,
                                "currentWeight", 70.0,
                                "logDate", LocalDate.now().toString()
                        ))))
                .andExpect(status().isOk());

        String adminToken = login("admin");

        mockMvc.perform(get("/api/admin/stats")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(2))
                .andExpect(jsonPath("$.newUsersLast7Days").value(2))
                .andExpect(jsonPath("$.activeUsersLast7Days").value(1))
                .andExpect(jsonPath("$.usersMissingCheckInToday").value(1))
                .andExpect(jsonPath("$.totalCheckIns").value(1))
                .andExpect(jsonPath("$.checkInsLast7Days").value(1))
                .andExpect(jsonPath("$.totalDietPlans").value(0))
                .andExpect(jsonPath("$.dietPlansLast7Days").value(0));
    }
}
