package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding // LOADING UI
    private lateinit var loadingTextView: TextView      // LOADING..

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater);
        setContentView(R.layout.activity_splash)

        loadingTextView = findViewById(R.id.loadingText)
        // LOADING 뒤에 ... 개수 변환
        animateLoadingDots()

        // 2초 후 MainActivity로 전환!
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish() // SplashActivity 종료
        }, 2000)
    }

    private fun animateLoadingDots() {
        val handler = Handler(Looper.getMainLooper())
        val dots = arrayOf(".", "..", "...", "....", "..")
        var index = 0

        val runnable = object : Runnable {
            override fun run() {
                loadingTextView.text = "LOADING" + dots[index % dots.size]
                index++
                handler.postDelayed(this, 400) // 0.4초
            }
        }

        handler.post(runnable)
    }
}
