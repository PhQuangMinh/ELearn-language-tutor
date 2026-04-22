package com.example.BTL_Mobile.controller;

import com.example.BTL_Mobile.dto.response.ApiResponse;
import com.example.BTL_Mobile.dto.response.StreakReminderTriggerResponse;
import com.example.BTL_Mobile.service.StreakReminderScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final StreakReminderScheduler streakReminderScheduler;

    @PostMapping("/streak-reminder/trigger")
    public ResponseEntity<ApiResponse<StreakReminderTriggerResponse>> triggerStreakReminder() {
        StreakReminderTriggerResponse result = streakReminderScheduler.triggerReminderNow();
        return ResponseEntity.ok(
                ApiResponse.success("Trigger gửi nhắc streak thành công!", result)
        );
    }
}
