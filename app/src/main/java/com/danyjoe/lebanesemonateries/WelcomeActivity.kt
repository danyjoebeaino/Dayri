package com.danyjoe.lebanesemonateries

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.danyjoe.lebanesemonateries.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding
    private val TAG = "WelcomeActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply the saved theme first, before inflating the layout
        val savedTheme = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .getInt("current_theme", UserSettings.THEME_SYSTEM)

        MonasteriesApp.applyTheme(savedTheme)

        // Only inflate the layout and set content view ONCE
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the Get Started button
        binding.btnGetStarted.setOnClickListener {
            Log.d(TAG, "Get Started button clicked")
            // Navigate to the login screen
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        Log.d(TAG, "WelcomeActivity onCreate completed")
    }
}