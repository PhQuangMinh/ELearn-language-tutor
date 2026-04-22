package com.example.BTL_Mobile.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class StreakReminderTriggerResponse {
    private LocalDate reminderDate;
    private int targetCount;
    private int sentCount;
}
