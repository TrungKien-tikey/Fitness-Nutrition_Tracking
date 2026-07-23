package com.fn_tracking.fitnessapi.service;

import com.fn_tracking.fitnessapi.dto.ActivityLevel;
import com.fn_tracking.fitnessapi.dto.HealthMetricRequest;
import com.fn_tracking.fitnessapi.entity.HealthMetric;
import com.fn_tracking.fitnessapi.entity.User;
import com.fn_tracking.fitnessapi.repository.HealthMetricRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthMetricServiceTest {

    @Mock
    private HealthMetricRepository healthMetricRepository;

    @InjectMocks
    private HealthMetricService healthMetricService;

    @Test
    void calculateAndSaveMetricsShouldCreateNewMetricWhenNoneExists() {
        HealthMetricRequest request = new HealthMetricRequest();
        request.setHeight(180.0);
        request.setWeight(80.0);
        request.setAge(30);
        request.setGender("MALE");
        request.setActivityLevel(ActivityLevel.SEDENTARY);

        User user = User.builder()
                .id(1L)
                .username("john")
                .email("john@example.com")
                .password("encoded-secret")
                .build();

        when(healthMetricRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(healthMetricRepository.save(any(HealthMetric.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HealthMetric savedMetric = healthMetricService.calculateAndSaveMetrics(request, user);

        assertEquals(user, savedMetric.getUser());
        assertEquals(180.0, savedMetric.getHeight());
        assertEquals(80.0, savedMetric.getWeight());
        assertEquals(24.7, savedMetric.getBmi());
        assertEquals(1853.6, savedMetric.getBmr());
        assertEquals(2224.4, savedMetric.getTdee());

        ArgumentCaptor<HealthMetric> captor = ArgumentCaptor.forClass(HealthMetric.class);
        verify(healthMetricRepository).save(captor.capture());
        assertEquals(user, captor.getValue().getUser());
    }

    @Test
    void calculateAndSaveMetricsShouldUpdateExistingMetric() {
        HealthMetricRequest request = new HealthMetricRequest();
        request.setHeight(165.0);
        request.setWeight(60.0);
        request.setAge(25);
        request.setGender("FEMALE");
        request.setActivityLevel(ActivityLevel.VERY_ACTIVE);

        User user = User.builder()
                .id(2L)
                .username("anna")
                .email("anna@example.com")
                .password("encoded-secret")
                .build();

        HealthMetric existingMetric = HealthMetric.builder().build();
        when(healthMetricRepository.findByUserId(2L)).thenReturn(Optional.of(existingMetric));
        when(healthMetricRepository.save(any(HealthMetric.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HealthMetric savedMetric = healthMetricService.calculateAndSaveMetrics(request, user);

        assertSame(existingMetric, savedMetric);
        assertEquals(user, savedMetric.getUser());
        assertEquals(165.0, savedMetric.getHeight());
        assertEquals(60.0, savedMetric.getWeight());
        assertEquals(22.0, savedMetric.getBmi());
        assertEquals(1405.3, savedMetric.getBmr());
        assertEquals(2424.2, savedMetric.getTdee());
    }

    @Test
    void calculateAndSaveMetricsShouldThrowExceptionWhenGenderIsInvalid() {
        // 1. Arrange: Tạo request với giới tính không hợp lệ
        HealthMetricRequest request = new HealthMetricRequest();
        request.setHeight(170.0);
        request.setWeight(70.0);
        request.setAge(25);
        request.setGender("ALIEN"); // Cố tình truyền sai
        request.setActivityLevel(ActivityLevel.SEDENTARY);

        User user = User.builder().id(3L).build();

        // 2 & 3. Act & Assert: Chạy hàm và kỳ vọng hệ thống phải ném ra lỗi
        // IllegalArgumentException
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            healthMetricService.calculateAndSaveMetrics(request, user);
        });
    }

    @Test
    void calculateAndSaveMetricsShouldThrowExceptionWhenHeightIsZeroOrNegative() {
        // 1. Arrange: Tạo request với chiều cao vô lý
        HealthMetricRequest request = new HealthMetricRequest();
        request.setHeight(0.0); // Chiều cao = 0
        request.setWeight(70.0);
        request.setAge(25);
        request.setGender("MALE");
        request.setActivityLevel(ActivityLevel.SEDENTARY);

        User user = User.builder().id(4L).build();

        // 2 & 3. Act & Assert: Kỳ vọng hệ thống ném ra lỗi dữ liệu không hợp lệ
        assertThrows(IllegalArgumentException.class, () -> {
            healthMetricService.calculateAndSaveMetrics(request, user);
        });
    }
}