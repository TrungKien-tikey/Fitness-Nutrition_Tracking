package com.fn_tracking.fitnessapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fn_tracking.fitnessapi.dto.DietPlanGenerationRequest;
import com.fn_tracking.fitnessapi.dto.FitnessGoal;
import com.fn_tracking.fitnessapi.entity.DietPlan;
import com.fn_tracking.fitnessapi.entity.HealthMetric;
import com.fn_tracking.fitnessapi.entity.User;
import com.fn_tracking.fitnessapi.repository.DietPlanRepository;
import com.fn_tracking.fitnessapi.repository.HealthMetricRepository;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DietPlanServiceTest {

    @Mock
    private ChatLanguageModel chatLanguageModel;

    @Mock
    private HealthMetricRepository healthMetricRepository;

    @Mock
    private DietPlanRepository dietPlanRepository;

    private ObjectMapper objectMapper;

    private DietPlanService dietPlanService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        dietPlanService = new DietPlanService(chatLanguageModel, objectMapper, healthMetricRepository,
                dietPlanRepository);
    }

    @Test
    void generateAndSaveWeeklyPlanShouldSaveValidatedJson() {
        User user = User.builder()
                .id(1L)
                .username("john")
                .email("john@example.com")
                .password("encoded-secret")
                .build();

        HealthMetric metric = HealthMetric.builder()
                .user(user)
                .height(180.0)
                .weight(80.0)
                .bmi(24.7)
                .bmr(1853.6)
                .tdee(2224.4)
                .build();

        DietPlanGenerationRequest request = new DietPlanGenerationRequest();
        request.setGoal(FitnessGoal.MUSCLE_GAIN);
        request.setDietaryNotes("No shellfish");
        request.setExcludedFoods(List.of("shellfish"));
        request.setPreferredCuisines(List.of("Asian"));

        when(healthMetricRepository.findByUserId(1L)).thenReturn(Optional.of(metric));
        when(chatLanguageModel.generate(anyString())).thenReturn(buildWeeklyPlanJson());
        when(dietPlanRepository.save(org.mockito.ArgumentMatchers.any(DietPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DietPlan savedPlan = dietPlanService.generateAndSaveWeeklyPlan(user, request);

        assertEquals(FitnessGoal.MUSCLE_GAIN, savedPlan.getGoal());
        assertTrue(savedPlan.getMenuJson().contains("\"days\""));
        verify(chatLanguageModel).generate(org.mockito.ArgumentMatchers.contains("target_daily_calories: 2524"));
        verify(dietPlanRepository).save(org.mockito.ArgumentMatchers.any(DietPlan.class));
    }

    @Test
    void generateAndSaveWeeklyPlanShouldRejectInvalidJson() {
        User user = User.builder().id(1L).username("john").build();
        HealthMetric metric = HealthMetric.builder().tdee(2200.0).build();

        DietPlanGenerationRequest request = new DietPlanGenerationRequest();
        request.setGoal(FitnessGoal.MAINTENANCE);

        when(healthMetricRepository.findByUserId(1L)).thenReturn(Optional.of(metric));
        when(chatLanguageModel.generate(anyString())).thenReturn("not-json");

        assertThrows(IllegalStateException.class, () -> dietPlanService.generateAndSaveWeeklyPlan(user, request));
        verify(dietPlanRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void generateAndSaveWeeklyPlanShouldRequireHealthMetric() {
        User user = User.builder().id(1L).username("john").build();

        DietPlanGenerationRequest request = new DietPlanGenerationRequest();
        request.setGoal(FitnessGoal.WEIGHT_LOSS);

        when(healthMetricRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> dietPlanService.generateAndSaveWeeklyPlan(user, request));
        verify(chatLanguageModel, never()).generate(anyString());
        verify(dietPlanRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private String buildWeeklyPlanJson() {
        StringBuilder builder = new StringBuilder();
        builder.append("```json\n{")
                .append("\n  \"goal\": \"MUSCLE_GAIN\",")
                .append("\n  \"targetDailyCalories\": 2524,")
                .append("\n  \"days\": [");

        for (int day = 1; day <= 7; day++) {
            builder.append("\n    {\n")
                    .append("      \"day\": ").append(day).append(",\n")
                    .append("      \"meals\": [\n")
                    .append("        {\"name\": \"Breakfast\", \"items\": [\"Oats\"], \"calories\": 500, \"proteinGrams\": 30, \"carbsGrams\": 50, \"fatGrams\": 15},\n")
                    .append("        {\"name\": \"Lunch\", \"items\": [\"Rice\"], \"calories\": 800, \"proteinGrams\": 45, \"carbsGrams\": 90, \"fatGrams\": 20},\n")
                    .append("        {\"name\": \"Dinner\", \"items\": [\"Chicken\"], \"calories\": 700, \"proteinGrams\": 50, \"carbsGrams\": 60, \"fatGrams\": 25}\n")
                    .append("      ],\n")
                    .append("      \"totalCalories\": 2000\n")
                    .append("    }");

            if (day < 7) {
                builder.append(",");
            }
        }

        builder.append("\n  ]\n}")
                .append("\n```");
        return builder.toString();
    }

    @Test
    void generateAndSaveWeeklyPlanShouldRejectJsonWhenDaysAreNotExactlySeven() {
        // 1. Arrange
        User user = User.builder().id(1L).username("john").build();
        HealthMetric metric = HealthMetric.builder().tdee(2200.0).build();

        DietPlanGenerationRequest request = new DietPlanGenerationRequest();
        request.setGoal(FitnessGoal.MAINTENANCE);

        when(healthMetricRepository.findByUserId(1L)).thenReturn(Optional.of(metric));

        // Giả lập AI trả về JSON hợp lệ nhưng CHỈ CÓ 1 NGÀY (thay vì 7 ngày)
        String incompleteJson = "{\n" +
                "  \"goal\": \"MAINTENANCE\",\n" +
                "  \"targetDailyCalories\": 2200,\n" +
                "  \"days\": [\n" +
                "    {\n" +
                "      \"day\": 1,\n" +
                "      \"meals\": [\n" +
                "        {\"name\": \"Breakfast\", \"items\": [\"Oats\"], \"calories\": 500, \"proteinGrams\": 30, \"carbsGrams\": 50, \"fatGrams\": 15},\n"
                +
                "        {\"name\": \"Lunch\", \"items\": [\"Rice\"], \"calories\": 800, \"proteinGrams\": 45, \"carbsGrams\": 90, \"fatGrams\": 20},\n"
                +
                "        {\"name\": \"Dinner\", \"items\": [\"Chicken\"], \"calories\": 700, \"proteinGrams\": 50, \"carbsGrams\": 60, \"fatGrams\": 25}\n"
                +
                "      ],\n" +
                "      \"totalCalories\": 2000\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        when(chatLanguageModel.generate(anyString())).thenReturn(incompleteJson);

        // 2. Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> dietPlanService.generateAndSaveWeeklyPlan(user, request));

        // Kiểm tra xem message lỗi có chứa thông điệp từ validatePlanSchema không
        assertTrue(exception.getMessage().contains("AI response is not a valid weekly diet plan JSON"));

        // 3. Verify: Đảm bảo rằng repository không bao giờ được gọi để lưu một thực đơn
        // lỗi
        verify(dietPlanRepository, never()).save(org.mockito.ArgumentMatchers.any(DietPlan.class));
    }
}