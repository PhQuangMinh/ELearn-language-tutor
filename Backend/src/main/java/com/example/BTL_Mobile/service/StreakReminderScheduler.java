package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.dto.response.StreakReminderTriggerResponse;
import com.example.BTL_Mobile.model.UserDeviceToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StreakReminderScheduler {

    @Value("${streak-reminder.enabled:false}")
    private boolean reminderEnabled;

    @Value("${streak-reminder.zone}")
    private String reminderZone;

    private final UserDeviceTokenService userDeviceTokenService;
    private final FcmNotificationService fcmNotificationService;

    @Scheduled(cron = "${streak-reminder.cron}", zone = "${streak-reminder.zone}")
    public void sendDailyReminder() {
        if (!reminderEnabled) {
            return;
        }

        StreakReminderTriggerResponse result = triggerReminderNow();
        log.info("Streak reminder job done. Date: {}, targets: {}, sent: {}",
                result.getReminderDate(),
                result.getTargetCount(),
                result.getSentCount());
    }

    public StreakReminderTriggerResponse triggerReminderNow() {
        ZoneId zoneId = ZoneId.of(reminderZone);
        LocalDate today = LocalDate.now(zoneId);
        LocalDateTime startOfDay = today.atStartOfDay();

        List<UserDeviceToken> targets = userDeviceTokenService.findReminderTargets(startOfDay);
        int sentCount = fcmNotificationService.sendDailyStreakReminder(targets, today);

        return StreakReminderTriggerResponse.builder()
                .reminderDate(today)
                .targetCount(targets.size())
                .sentCount(sentCount)
                .build();
    }
}
