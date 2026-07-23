package com.fn_tracking.fitnessapi.service;

import com.fn_tracking.fitnessapi.dto.DailyLogCheckInRequest;
import com.fn_tracking.fitnessapi.entity.DailyLog;
import com.fn_tracking.fitnessapi.entity.User;
import com.fn_tracking.fitnessapi.repository.DailyLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyLogServiceTest {

    @Mock
    private DailyLogRepository dailyLogRepository;

    @InjectMocks
    private DailyLogService dailyLogService;

    @Test
    void checkInShouldCreateNewLogWhenNoLogExistsForDate() {
        DailyLogCheckInRequest request = new DailyLogCheckInRequest();
        request.setCaloriesIn(2200);
        request.setCurrentWeight(72.5);
        request.setLogDate(LocalDate.of(2026, 7, 23));

        User user = User.builder()
                .id(10L)
                .username("john")
                .email("john@example.com")
                .password("encoded-secret")
                .build();

        when(dailyLogRepository.findByUserIdAndLogDate(10L, request.getLogDate())).thenReturn(Optional.empty());
        when(dailyLogRepository.save(any(DailyLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DailyLog savedLog = dailyLogService.checkIn(request, user);

        assertEquals(user, savedLog.getUser());
        assertEquals(request.getLogDate(), savedLog.getLogDate());
        assertEquals(2200, savedLog.getCaloriesIn());
        assertEquals(72.5, savedLog.getCurrentWeight());

        ArgumentCaptor<DailyLog> captor = ArgumentCaptor.forClass(DailyLog.class);
        verify(dailyLogRepository).save(captor.capture());
        assertEquals(user, captor.getValue().getUser());
    }

    @Test
    void checkInShouldUpdateExistingLogForSameDay() {
        DailyLogCheckInRequest request = new DailyLogCheckInRequest();
        request.setCaloriesIn(1800);
        request.setCurrentWeight(71.0);
        request.setLogDate(LocalDate.of(2026, 7, 23));

        User user = User.builder()
                .id(10L)
                .username("john")
                .email("john@example.com")
                .password("encoded-secret")
                .build();

        DailyLog existingLog = DailyLog.builder()
                .id(99L)
                .user(user)
                .logDate(request.getLogDate())
                .caloriesIn(1500)
                .currentWeight(73.0)
                .build();

        when(dailyLogRepository.findByUserIdAndLogDate(10L, request.getLogDate())).thenReturn(Optional.of(existingLog));
        when(dailyLogRepository.save(any(DailyLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DailyLog savedLog = dailyLogService.checkIn(request, user);

        assertSame(existingLog, savedLog);
        assertEquals(1800, savedLog.getCaloriesIn());
        assertEquals(71.0, savedLog.getCurrentWeight());
    }

    @Test
    void getRecentLogsShouldRejectInvalidDays() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> dailyLogService.getRecentLogs(10L, 0));

        assertEquals("Days must be greater than 0", exception.getMessage());
        verifyNoInteractions(dailyLogRepository);
    }

    @Test
    void getRecentLogsShouldReturnLogsFromCalculatedDateRange() {
        // Thay vì fix cứng ngày, tính toán tự động dựa trên ngày hiện tại
        int daysToFetch = 7;
        LocalDate expectedFromDate = LocalDate.now().minusDays(daysToFetch - 1L);
        DailyLog log = DailyLog.builder().id(1L).build();

        when(dailyLogRepository.findByUserIdAndLogDateGreaterThanEqualOrderByLogDateDesc(10L, expectedFromDate))
                .thenReturn(List.of(log));

        List<DailyLog> logs = dailyLogService.getRecentLogs(10L, daysToFetch);

        assertEquals(1, logs.size());
        assertSame(log, logs.get(0));
        verify(dailyLogRepository).findByUserIdAndLogDateGreaterThanEqualOrderByLogDateDesc(10L, expectedFromDate);
    }
}