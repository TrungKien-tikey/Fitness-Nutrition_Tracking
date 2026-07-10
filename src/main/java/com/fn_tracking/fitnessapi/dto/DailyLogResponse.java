package com.fn_tracking.fitnessapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyLogResponse {
    private Long id;
    private LocalDate logDate;
    private Integer caloriesIn;
    private Double currentWeight;
}