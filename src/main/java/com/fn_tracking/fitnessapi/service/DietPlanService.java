package com.fn_tracking.fitnessapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fn_tracking.fitnessapi.dto.DietPlanGenerationRequest;
import com.fn_tracking.fitnessapi.dto.FitnessGoal;
import com.fn_tracking.fitnessapi.entity.DietPlan;
import com.fn_tracking.fitnessapi.entity.HealthMetric;
import com.fn_tracking.fitnessapi.entity.User;
import com.fn_tracking.fitnessapi.repository.DietPlanRepository;
import com.fn_tracking.fitnessapi.repository.HealthMetricRepository;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor
public class DietPlanService {

    private final ChatLanguageModel chatLanguageModel;
    private final ObjectMapper objectMapper;
    private final HealthMetricRepository healthMetricRepository;
    private final DietPlanRepository dietPlanRepository;

    public DietPlan generateAndSaveWeeklyPlan(User user, DietPlanGenerationRequest request) {
        HealthMetric healthMetric = healthMetricRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("Please calculate health metrics before generating a diet plan"));

        String prompt = buildPrompt(user, healthMetric, request);
        String modelResponse = chatLanguageModel.generate(prompt);
        String normalizedJson = normalizeAndValidatePlanJson(modelResponse);

        DietPlan dietPlan = DietPlan.builder()
                .user(user)
                .goal(request.getGoal())
                .menuJson(normalizedJson)
                .build();

        return dietPlanRepository.save(dietPlan);
    }

    public DietPlan getLatestPlan(Long userId) {
        return dietPlanRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new IllegalStateException("No diet plan found for this user"));
    }

    private String buildPrompt(User user, HealthMetric healthMetric, DietPlanGenerationRequest request) {
        int targetCalories = calculateTargetCalories(healthMetric.getTdee(), request.getGoal());
        StringJoiner excludedFoods = new StringJoiner(", ");
        Optional.ofNullable(request.getExcludedFoods())
                .orElse(List.of())
                .forEach(excludedFoods::add);

        StringJoiner preferredCuisines = new StringJoiner(", ");
        Optional.ofNullable(request.getPreferredCuisines())
                .orElse(List.of())
                .forEach(preferredCuisines::add);

        String notes = request.getDietaryNotes() == null || request.getDietaryNotes().isBlank()
                ? "None"
                : request.getDietaryNotes();

        return "You are a nutrition planner for fitness users. " +
                "Generate ONLY valid JSON for a 7-day meal plan. " +
                "Do not include markdown, explanations, or code fences.\n\n" +
                "User profile:\n" +
                "- username: " + user.getUsername() + "\n" +
                "- height_cm: " + healthMetric.getHeight() + "\n" +
                "- weight_kg: " + healthMetric.getWeight() + "\n" +
                "- bmi: " + healthMetric.getBmi() + "\n" +
                "- bmr: " + healthMetric.getBmr() + "\n" +
                "- tdee: " + healthMetric.getTdee() + "\n" +
                "- goal: " + request.getGoal() + "\n" +
                "- target_daily_calories: " + targetCalories + "\n" +
                "- excluded_foods: " + (excludedFoods.length() == 0 ? "None" : excludedFoods) + "\n" +
                "- preferred_cuisines: " + (preferredCuisines.length() == 0 ? "Any" : preferredCuisines) + "\n" +
                "- dietary_notes: " + notes + "\n\n" +
                "Return JSON in this schema:\n" +
                "{\n" +
                "  \"goal\": \"" + request.getGoal() + "\",\n" +
                "  \"targetDailyCalories\": 0,\n" +
                "  \"days\": [\n" +
                "    {\n" +
                "      \"day\": 1,\n" +
                "      \"meals\": [\n" +
                "        {\"name\": \"Breakfast\", \"items\": [\"...\"], \"calories\": 0, \"proteinGrams\": 0, \"carbsGrams\": 0, \"fatGrams\": 0}\n" +
                "      ],\n" +
                "      \"totalCalories\": 0\n" +
                "    }\n" +
                "  ]\n" +
                "}\n" +
                "Rules: exactly 7 days, each day must include at least 3 meals and daily calories should stay close to targetDailyCalories.";
    }

    private int calculateTargetCalories(Double tdee, FitnessGoal goal) {
        return (int) Math.round(tdee + goal.getCalorieAdjustment());
    }

    private String normalizeAndValidatePlanJson(String rawResponse) {
        String cleaned = stripMarkdownCodeFences(rawResponse).trim();
        try {
            JsonNode root = objectMapper.readTree(cleaned);
            validatePlanSchema(root);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception ex) {
            throw new IllegalStateException("AI response is not a valid weekly diet plan JSON", ex);
        }
    }

    private String stripMarkdownCodeFences(String content) {
        if (content == null) {
            throw new IllegalStateException("AI response is empty");
        }

        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int firstNewLine = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNewLine >= 0 && lastFence > firstNewLine) {
                return trimmed.substring(firstNewLine + 1, lastFence).trim();
            }
        }

        return trimmed;
    }

    private void validatePlanSchema(JsonNode root) {
        if (!root.isObject()) {
            throw new IllegalStateException("Plan JSON must be an object");
        }

        JsonNode daysNode = root.get("days");
        if (daysNode == null || !daysNode.isArray() || daysNode.size() != 7) {
            throw new IllegalStateException("Plan JSON must contain exactly 7 days");
        }

        for (JsonNode dayNode : daysNode) {
            if (!dayNode.isObject()) {
                throw new IllegalStateException("Each day must be an object");
            }
            if (!dayNode.hasNonNull("day") || !dayNode.get("day").canConvertToInt()) {
                throw new IllegalStateException("Each day must contain an integer day number");
            }
            JsonNode mealsNode = dayNode.get("meals");
            if (mealsNode == null || !mealsNode.isArray() || mealsNode.size() < 3) {
                throw new IllegalStateException("Each day must contain at least 3 meals");
            }
        }
    }
}