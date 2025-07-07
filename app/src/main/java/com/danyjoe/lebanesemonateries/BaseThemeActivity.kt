package com.danyjoe.lebanesemonateries

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

open class BaseThemeActivity : AppCompatActivity() {
    private var currentNightMode: Int = AppCompatDelegate.getDefaultNightMode()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Store the current night mode
        currentNightMode = AppCompatDelegate.getDefaultNightMode()
    }

    override fun onResume() {
        super.onResume()
        // Check if night mode has changed
        if (currentNightMode != AppCompatDelegate.getDefaultNightMode()) {
            recreate()
        }
    }
}