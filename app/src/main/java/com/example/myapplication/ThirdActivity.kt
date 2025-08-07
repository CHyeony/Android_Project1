package com.example.myapplication

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings.System.putInt
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.example.myapplication.databinding.ActivitySplashBinding
import com.example.myapplication.databinding.ActivityThirdBinding
import com.example.mypomodorotimer.NotificationHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class ThirdActivity : AppCompatActivity() {


    private lateinit var binding: ActivityThirdBinding
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
        /* 근데 간헐 단식 16:8 12시간 (*60분)
         16시간 단식 + 8시간 안에 식사 (나중에 단식 시간 설정값으로 변경예정)
        * */
        private const val WORK_TIME_MINUTES = 960L
        private const val BREAK_TIME_MINUTES = 480L
        // 4번째 포모도로 후 긴 휴식
        private const val LONG_BREAK_TIME_MINUTES = 60L
        private const val PREFS_NAME = "PomodoroPrefs"
        private const val KEY_TIMER_STATE = "timerState"
        private const val KEY_SECONDS_REMAINING = "secondsRemaining"
        private const val KEY_TIMER_LENGTH = "timerLength"
        private const val KEY_IS_WORK_TIME = "isWorkTime"
        private const val KEY_POMODORO_COUNT = "pomodoroCount"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Activity 생명주기: 처음 생성될 때 1번만 호출
        // View 초기화, 리스너 설정, setContentView() 호출
        binding = ActivityThirdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 이미지
        val imageView = binding.imageViewCenter

    // Android O부터 NotificationChannel 필수
        createNotificationChannel()
        notificationHelper = NotificationHelper(this)


        // Android 13부터 명시적 알림 권한 요청 필수
        checkNotificationPermission()

        // 초기 배경색 설정
        binding.root.setBackgroundColor(ContextCompat.getColor(this, R.color.darkGray))

        // 버튼 리스너 설정
        setupClickListeners()

        // SharedPreferences에서 저장된 상태 복원
        restoreTimerState()
    }


    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // 권한이 이미 허용됨
                }
                else -> {
                    // 권한 요청
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabPlayPause.setOnClickListener {
            when (timerState) {
                TimerState.STOPPED -> {
                    startTimer()
                    timerState = TimerState.RUNNING
                    updateButtons()
                }
                TimerState.RUNNING -> {
                    timer?.cancel()
                    timerState = TimerState.PAUSED
                    updateButtons()
                }
                TimerState.PAUSED -> {
                    startTimer()
                    timerState = TimerState.RUNNING
                    updateButtons()
                }
            }
        }

        binding.fabStop.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.stop_timer_title))
                .setMessage(getString(R.string.stop_timer_message))
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    timer?.cancel()
                    onTimerFinished(true)
                }
                .setNegativeButton(getString(R.string.no), null)
                .show()
        }

        binding.fabSkip.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.skip_timer_title))
                .setMessage(getString(R.string.skip_timer_message))
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    timer?.cancel()
                    onTimerFinished(false)
                }
                .setNegativeButton(getString(R.string.no), null)
                .show()
        }
    }

    private fun startTimer() {
        // CountDownTimer: Android 기본 제공 카운트다운 클래스
        // millisInFuture: 전체 시간 (밀리초)
        // countDownInterval: 업데이트 간격 (보통 1000ms = 1초)
        timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // 매 간격마다 호출되어 UI 업데이트
                secondsRemaining = millisUntilFinished / 1000
                updateCountdownUI()
            }

            override fun onFinish() {
                // 타이머 완료 시 호출
                onTimerFinished(false)
            }
        }.start()
    }

    private fun onTimerFinished(isCancelled: Boolean) {
        // 알림음 재생
        if (!isCancelled) {
            playSound()

            // 진동 재생
            vibrate()

            // 작업 완료 시 포모도로 카운트 증가
            if (isWorkTime) {
                pomodoroCount++
                binding.textPomodoroCount.text = getString(R.string.pomodoro_count_format, pomodoroCount)
            }

            // 알림 표시
            val message = if (isWorkTime) {
                getString(R.string.work_complete_message)
            } else {
                getString(R.string.break_complete_message)
            }
            notificationHelper.showTimerFinishedNotification(message)

            // 다음 세션 준비
            switchToNextSession()
        } else {
            // 타이머가 취소된 경우
            timerState = TimerState.STOPPED
            secondsRemaining = 0
            updateCountdownUI()
            updateButtons()
        }
    }

    private fun switchToNextSession() {
        // 작업/휴식 전환
        isWorkTime = !isWorkTime

        // 타이머 길이 설정
        timerLengthSeconds = if (isWorkTime) {
            WORK_TIME_MINUTES * 60
        } else {
            // 4번째 포모도로 후에는 긴 휴식
            if (pomodoroCount % 4 == 0 && pomodoroCount > 0) {
                LONG_BREAK_TIME_MINUTES * 60
            } else {
                BREAK_TIME_MINUTES * 60
            }
        }

        secondsRemaining = timerLengthSeconds
        timerState = TimerState.STOPPED

        updateUI()
        updateButtons()
    }

    private fun updateUI() {
        // 세션 타입 표시
        binding.textSessionType.text = if (isWorkTime) {
            getString(R.string.work_time)
        } else {
            if (pomodoroCount % 4 == 0 && pomodoroCount > 0) {
                getString(R.string.long_break_time)
            } else {
                getString(R.string.break_time)
            }
        }


        // 배경 변경 CUSTOM
        val imageResId = if (isWorkTime) {
            R.drawable.hungry_img  // 단식 기간 이미지
        } else {
            R.drawable.eat_img     // 단식 끝 이미지 밥먹어
        }

        binding.imageViewCenter.setImageResource(imageResId)
//        // 배경색 변경
//        val backgroundColor = if (isWorkTime) {
//            ContextCompat.getColor(this, R.color.darkGray)
//        } else {
//            ContextCompat.getColor(this, R.color.break_background)
//        }
//        binding.root.setBackgroundColor(backgroundColor)


        updateCountdownUI()
    }

    private fun updateCountdownUI() {
        val hours = secondsRemaining / 3600
        val minutes = (secondsRemaining % 3600) / 60
        val seconds = secondsRemaining % 60


        binding.textCountdown.text = getString(R.string.timer_format_hms, hours, minutes, seconds)

        // 프로그레스 업데이트
        // 올바른 계산식: 0에서 100으로 증가
        val progress = if (timerLengthSeconds > 0) {
            ((timerLengthSeconds - secondsRemaining).toFloat() / timerLengthSeconds * 100).toInt()
        } else {
            0
        }
        binding.progressCountdown.progress = progress
    }

    private fun updateButtons() {
        when (timerState) {
            TimerState.RUNNING -> {
                binding.fabPlayPause.setImageResource(android.R.drawable.ic_media_pause)
                binding.fabStop.isEnabled = true
                binding.fabSkip.visibility = View.VISIBLE
            }
            TimerState.STOPPED -> {
                binding.fabPlayPause.setImageResource(android.R.drawable.ic_media_play)
                binding.fabStop.isEnabled = false
                binding.fabSkip.visibility = View.GONE
            }
            TimerState.PAUSED -> {
                binding.fabPlayPause.setImageResource(android.R.drawable.ic_media_play)
                binding.fabStop.isEnabled = true
                binding.fabSkip.visibility = View.VISIBLE
            }
        }
    }

    private fun playSound() {
        try {
            // 기본 알림음 사용
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(applicationContext, notification)
            ringtone.play()
        } catch (_: Exception) {
            // 사운드 재생 실패 시 무시
            Toast.makeText(this, getString(R.string.sound_play_error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun vibrate() {
        // 진동 권한 체크
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE)
            == PackageManager.PERMISSION_GRANTED) {
            try {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    getSystemService(VIBRATOR_SERVICE) as Vibrator
                }
                vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
            } catch (e: SecurityException) {
                // 진동 권한이 없는 경우 무시
                e.printStackTrace()
            }
        }
    }

    // Activity 생명주기 메서드들-----------------------------------------------------
    override fun onResume() {
        super.onResume()
        // Activity 생명주기: 사용자와 상호작용 시작
        // 타이머 상태 복원, UI 업데이트

        // 백그라운드에서 돌아왔을 때 UI 업데이트
        if (timerState == TimerState.RUNNING) {
            // 타이머가 실행 중이었다면 계속 실행
            updateCountdownUI()
        }
    }

    override fun onPause() {
        super.onPause()
        // Activity 생명주기: 포커스 잃을 때
        // 타이머 상태 저장, 백그라운드 알림 표시

        // SharedPreferences에 상태 저장
        saveTimerState()

        // 백그라운드로 갈 때 실행 중인 타이머가 있다면 알림 표시
        if (timerState == TimerState.RUNNING) {
            notificationHelper.showTimerRunningNotification(
                secondsRemaining,
                if (isWorkTime) getString(R.string.work_in_progress) else getString(R.string.break_in_progress)
            )
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        // Activity 생명주기: 완전히 종료
        // 리소스 정리 필수 - 메모리 누수 방지
        timer?.cancel()
    }

    private fun saveTimerState() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit {
            /* [ putInt IMPORT MEMO ]
            *  1) Settings.System : 밝기, 소리 크기등 단말기 사용자 설정 값 저장 및 읽을때 씀. (앱이 WRITE_SETTING 권한 받으면 수정가능)
            *  2) Settings.Secure : 보안 상 앱이 수정할 수 없는 값들. READ ONLY
            *  3) Settings.Global : 4.2 버전 이상. 다중 사용자 환경에서 전체 디바이스에 적용되는 설정 관라. (SYSTEM LEVEL권한 / 관리자느낌)
            * */
            putInt(KEY_TIMER_STATE, timerState.ordinal)
            putLong(KEY_SECONDS_REMAINING, secondsRemaining)
            putLong(KEY_TIMER_LENGTH, timerLengthSeconds)
            putBoolean(KEY_IS_WORK_TIME, isWorkTime)
            putInt(KEY_POMODORO_COUNT, pomodoroCount)
        }
    }

    private fun createNotificationChannel() {
        // Min SDK 28이므로 O 버전 체크 불필요
        val channel = NotificationChannel(
            NotificationHelper.CHANNEL_ID,
            getString(R.string.app_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.app_name)
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun restoreTimerState() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        timerState = TimerState.entries[prefs.getInt(KEY_TIMER_STATE, 0)]
        secondsRemaining = prefs.getLong(KEY_SECONDS_REMAINING, 0)
        timerLengthSeconds = prefs.getLong(KEY_TIMER_LENGTH, WORK_TIME_MINUTES * 60)
        isWorkTime = prefs.getBoolean(KEY_IS_WORK_TIME, true)
        pomodoroCount = prefs.getInt(KEY_POMODORO_COUNT, 0)

        // 초기 설정
//        if (secondsRemaining == 0L) {
//            secondsRemaining = WORK_TIME_MINUTES * 60
//            timerLengthSeconds = WORK_TIME_MINUTES * 60
//        }
// 저장 값이 0일 때만 적용...
        if (timerState == TimerState.STOPPED) {

            timerLengthSeconds = WORK_TIME_MINUTES * 60
            secondsRemaining = WORK_TIME_MINUTES * 60
        } else {

            secondsRemaining = prefs.getLong(KEY_SECONDS_REMAINING, WORK_TIME_MINUTES * 60)
            timerLengthSeconds = prefs.getLong(KEY_TIMER_LENGTH, WORK_TIME_MINUTES * 60)
        }

        binding.textPomodoroCount.text = getString(R.string.pomodoro_count_format, pomodoroCount)
        updateUI()
        updateButtons()

        // 타이머가 실행 중이었다면 재시작
        if (timerState == TimerState.RUNNING) {
            startTimer()
        }
    }
}