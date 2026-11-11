package com.fn_tracking.fitnessapi.integration.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fn_tracking.fitnessapi.entity.Role;
import com.fn_tracking.fitnessapi.entity.User;
import com.fn_tracking.fitnessapi.repository.DailyLogRepository;
import com.fn_tracking.fitnessapi.repository.DietPlanRepository;
import com.fn_tracking.fitnessapi.repository.HealthMetricRepository;
import com.fn_tracking.fitnessapi.repository.UserRepository;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integration")
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    private DailyLogRepository dailyLogRepository;

    @Autowired
    private DietPlanRepository dietPlanRepository;

    @Autowired
    private HealthMetricRepository healthMetricRepository;

    @MockitoBean
    protected ChatLanguageModel chatLanguageModel;

    @BeforeEach
    void cleanDatabase() {
        dailyLogRepository.deleteAll();
        dietPlanRepository.deleteAll();
        healthMetricRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected User createUser(String username, Role role, boolean enabled) {
        return userRepository.save(TestDataFactory.user(username, role, enabled, passwordEncoder));
    }

    protected User createAdmin(String username) {
        return createUser(username, Role.ADMIN, true);
    }

    protected void register(String username) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestDataFactory.registrationBody(username))))
                .andExpect(status().isOk());
    }

    protected String login(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestDataFactory.loginBody(username))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token")
                .asText();
    }

    protected String registerAndLogin(String username) throws Exception {
        register(username);
        return login(username);
    }
}
