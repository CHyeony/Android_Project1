package com.example.mypomodorotimer

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.ThirdActivity
import java.util.Locale

/**
 * 알림 관련 기능을 처리하는 헬퍼 클래스
 * 타이머 완료 알림, 백그라운드 실행 알림 등을 관리
 */
class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "pomodoro_timer_channel"
        private const val TIMER_RUNNING_ID = 1001
        private const val TIMER_FINISHED_ID = 1002
    }

    /**
     * 타이머가 백그라운드에서 실행 중일 때 표시하는 알림
     */
    fun showTimerRunningNotification(secondsRemaining: Long, sessionType: String) {
        val intent = Intent(context, ThirdActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val minutes = secondsRemaining / 60
        val seconds = secondsRemaining % 60
        val timeText = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(context.getString(R.string.timer_running_title))
            .setContentText(context.getString(R.string.timer_notification_format, sessionType, timeText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // 사용자가 스와이프로 제거할 수 없도록
            .build()

        // 알림 권한 체크
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            try {
                with(NotificationManagerCompat.from(context)) {
                    notify(TIMER_RUNNING_ID, notification)
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 타이머가 완료되었을 때 표시하는 알림
     */
    fun showTimerFinishedNotification(message: String) {
        // 실행 중 알림 제거
        cancelTimerRunningNotification()

        val intent = Intent(context, ThirdActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // 탭하면 자동으로 제거
            .setDefaults(Notification.DEFAULT_ALL) // 기본 소리, 진동 사용
            .build()

        // 알림 권한 체크
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED) {
            try {
                with(NotificationManagerCompat.from(context)) {
                    notify(TIMER_FINISHED_ID, notification)
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 실행 중 알림 제거
     */
    fun cancelTimerRunningNotification() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(TIMER_RUNNING_ID)
    }
}