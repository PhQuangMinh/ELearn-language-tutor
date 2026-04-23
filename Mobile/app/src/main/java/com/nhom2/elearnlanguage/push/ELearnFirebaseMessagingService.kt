package com.nhom2.elearnlanguage.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.domain.usecase.SyncFcmTokenUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ELearnFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var syncFcmTokenUseCase: SyncFcmTokenUseCase

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                syncFcmTokenUseCase(token)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: "ELearn"
        val body = message.notification?.body ?: "Bạn có thông báo mới"

        createNotificationChannel()
        showNotification(title, body)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val soundUri = getStreakReminderSoundUri()

        val channel = NotificationChannel(
            CHANNEL_ID,
            "ELearn reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Streak reminder notifications"
            enableVibration(true)
            vibrationPattern = VIBRATION_PATTERN
            setSound(
                soundUri,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
        }

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun showNotification(title: String, body: String) {
        val soundUri = getStreakReminderSoundUri()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_icon)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSound(soundUri)
            .setVibrate(VIBRATION_PATTERN)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun getStreakReminderSoundUri(): Uri {
        return Uri.parse("android.resource://$packageName/${R.raw.correct_answer}")
    }

    companion object {
        private const val CHANNEL_ID = "streak_reminder_v2"
        private val VIBRATION_PATTERN = longArrayOf(0, 250, 200, 250)
    }
}
