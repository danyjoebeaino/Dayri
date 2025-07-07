package com.danyjoe.lebanesemonateries

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MonasteriesApp : Application() {
    companion object {
        // Add a method to apply theme that can be called from anywhere
        fun applyTheme(themeMode: Int) {
            val nightMode = when (themeMode) {
                UserSettings.THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                UserSettings.THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Load monastery translations
        CoroutineScope(Dispatchers.IO).launch {
            TranslationManager.loadMonasteryTranslations()
        }

        // Load theme settings if user is logged in
        loadUserTheme()
    }

    private fun loadUserTheme() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser ?: return

        // Use coroutine to load theme setting
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val doc = db.collection("userSettings").document(currentUser.uid).get().await()

                if (doc.exists()) {
                    val themeMode = (doc.getLong("themeMode") ?: UserSettings.THEME_SYSTEM).toInt()
                    applyTheme(themeMode)
                }
            } catch (e: Exception) {
                // Default to system if there's an error
                applyTheme(UserSettings.THEME_SYSTEM)
            }
        }
    }

    override fun attachBaseContext(base: Context) {
        // Apply saved language
        super.attachBaseContext(LocaleHelper.setLocale(base))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Apply saved language on configuration change
        LocaleHelper.setLocale(this)
    }
}