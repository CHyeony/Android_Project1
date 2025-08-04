package com.example.myapplication

import android.os.CountDownTimer
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivitySplashBinding
import com.example.mypomodorotimer.NotificationHelper



class ThirdActivity : AppCompatActivity{


    private lateinit var binding: ActivitySplashBinding
    private lateinit var notificationHelper: NotificationHelper

    // TIMER 관련 변수
    private var timer: CountDownTimer? = null
    private var timerState = TimerState.STOPPED
    private var timerLengthSeconds = 0L
    private var secondsRemaining = 0L

    // 포모도로 사이클 관련 변수
    private var pomodoroCount = 0
    private var isWorkTime = true

    // 알림 권한 요청을 위한 런처
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, getString(R.string.notification_permission_granted), Toast.LENGTH_SHORT).show()
        }
    }


    companion object {
        // 테스트 시 1L로 변경 가능, 실제 사용 시 25L
        private const val WORK_TIME_MINUTES = 25L
        private const val BREAK_TIME_MINUTES = 5L
        // 4번째 포모도로 후 긴 휴식
        private const val LONG_BREAK_TIME_MINUTES = 15L
        private const val PREFS_NAME = "PomodoroPrefs"
        private const val KEY_TIMER_STATE = "timerState"
        private const val KEY_SECONDS_REMAINING = "secondsRemaining"
        private const val KEY_TIMER_LENGTH = "timerLength"
        private const val KEY_IS_WORK_TIME = "isWorkTime"
        private const val KEY_POMODORO_COUNT = "pomodoroCount"
    }


}