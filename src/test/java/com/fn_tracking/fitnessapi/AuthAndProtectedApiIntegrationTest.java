package com.fn_tracking.fitnessapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthAndProtectedApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChatLanguageModel chatLanguageModel;

    @Test
    void registerThenLoginReturnsToken() throws Exception {
        register("it_auth_user");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "it_auth_user",
                                "password", "secret123"
                        ))))
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
                        .content(healthMetricBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    void authenticatedUserCanCalculateMetricsAndCheckIn() throws Exception {
        register("it_flow_user");
        String token = login("it_flow_user");

        mockMvc.perform(post("/api/health-metrics/calculate")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(healthMetricBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bmi").isNumber())
                .andExpect(jsonPath("$.bmr").isNumber())
                .andExpect(jsonPath("$.tdee").isNumber());

        mockMvc.perform(post("/api/daily-logs/check-in")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
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

    private String healthMetricBody() throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "height", 170.0,
                "weight", 65.0,
                "age", 25,
                "gender", "MALE",
                "activityLevel", "MODERATELY_ACTIVE"
        ));
    }
}
