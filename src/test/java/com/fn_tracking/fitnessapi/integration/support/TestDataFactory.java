package com.fn_tracking.fitnessapi.integration.support;

import com.fn_tracking.fitnessapi.entity.Role;
import com.fn_tracking.fitnessapi.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

public final class TestDataFactory {

    public static final String PASSWORD = "secret123";

    private TestDataFactory() {
    }

    public static Map<String, Object> registrationBody(String username) {
        return Map.of(
                "username", username,
                "email", username + "@example.com",
                "password", PASSWORD
        );
    }

    public static Map<String, Object> loginBody(String username) {
        return Map.of(
                "username", username,
                "password", PASSWORD
        );
    }

    public static Map<String, Object> healthMetricBody() {
        return Map.of(
                "height", 170.0,
                "weight", 65.0,
                "age", 25,
                "gender", "MALE",
                "activityLevel", "MODERATELY_ACTIVE"
        );
    }

    public static User user(String username, Role role, boolean enabled, PasswordEncoder passwordEncoder) {
        return User.builder()
                .username(username)
                .email(username + "@example.com")
                .password(passwordEncoder.encode(PASSWORD))
                .role(role)
                .enabled(enabled)
                .build();
    }
}
