package com.fn_tracking.fitnessapi.integration.auth;

import com.fn_tracking.fitnessapi.integration.support.AbstractIntegrationTest;
import com.fn_tracking.fitnessapi.integration.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthAndProtectedApiIntegrationTest extends AbstractIntegrationTest {

    @Test
    void registerThenLoginReturnsToken() throws Exception {
        register("it_auth_user");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                TestDataFactory.loginBody("it_auth_user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("it_auth_user"))
                .andExpect(jsonPath("$.email").value("it_auth_user@example.com"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String token = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token")
                .asText();
        assertFalse(token.isBlank());
    }

    @Test
    void protectedEndpointRejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(post("/api/health-metrics/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestDataFactory.healthMetricBody())))
                .andExpect(status().isForbidden());
    }

    @Test
    void authenticatedUserCanCalculateMetricsAndCheckIn() throws Exception {
        String token = registerAndLogin("it_flow_user");

        mockMvc.perform(post("/api/health-metrics/calculate")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestDataFactory.healthMetricBody())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bmi").isNumber())
                .andExpect(jsonPath("$.bmr").isNumber())
                .andExpect(jsonPath("$.tdee").isNumber());

        mockMvc.perform(post("/api/daily-logs/check-in")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "caloriesIn", 2100,
                                "currentWeight", 70.0
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caloriesIn").value(2100))
                .andExpect(jsonPath("$.currentWeight").value(70.0));

        mockMvc.perform(get("/api/daily-logs/history")
                        .param("days", "7")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].caloriesIn").value(2100));
    }
}
