package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.model.UserDeviceToken;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmNotificationService {

    private final ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;
    private final UserDeviceTokenService userDeviceTokenService;

    public int sendDailyStreakReminder(List<UserDeviceToken> deviceTokens, LocalDate reminderDate) {
        if (deviceTokens == null || deviceTokens.isEmpty()) {
            return 0;
        }

        FirebaseMessaging firebaseMessaging = firebaseMessagingProvider.getIfAvailable();
        if (firebaseMessaging == null) {
            log.warn("Skip FCM send because Firebase credentials are not configured.");
            return 0;
        }

        List<String> fcmTokens = deviceTokens.stream()
                .map(UserDeviceToken::getFcmToken)
                .toList();

        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle("Đừng quên bài học hôm nay")
                        .setBody("Bạn chưa làm bài hôm nay. Hoàn thành 1 bài để giữ streak nhé!")
                        .build())
            .setAndroidConfig(AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder()
                    .setChannelId("streak_reminder_v2")
                    .setSound("default")
                    .build())
                .build())
            .setApnsConfig(ApnsConfig.builder()
                .setAps(Aps.builder()
                    .setSound("default")
                    .build())
                .build())
                .putData("type", "STREAK_REMINDER")
                .putData("date", reminderDate.toString())
                .putData("deep_link", "elearn://home")
                .addAllTokens(fcmTokens)
                .build();

        try {
            BatchResponse batchResponse = firebaseMessaging.sendEachForMulticast(message);
            int successCount = batchResponse.getSuccessCount();

            List<SendResponse> responses = batchResponse.getResponses();
            for (int i = 0; i < responses.size(); i++) {
                SendResponse response = responses.get(i);
                if (response.isSuccessful()) {
                    continue;
                }

                FirebaseMessagingException exception = response.getException();
                if (shouldDeactivateToken(exception)) {
                    userDeviceTokenService.deactivateToken(fcmTokens.get(i));
                }
            }

            return successCount;
        } catch (FirebaseMessagingException ex) {
            log.error("Failed to send FCM streak reminder: {}", ex.getMessage(), ex);
            return 0;
        }
    }

    private boolean shouldDeactivateToken(FirebaseMessagingException exception) {
        if (exception == null || exception.getMessagingErrorCode() == null) {
            return false;
        }
        MessagingErrorCode errorCode = exception.getMessagingErrorCode();
        return errorCode == MessagingErrorCode.UNREGISTERED
                || errorCode == MessagingErrorCode.INVALID_ARGUMENT;
    }
}
