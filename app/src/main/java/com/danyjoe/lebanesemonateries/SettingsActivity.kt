package com.danyjoe.lebanesemonateries

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.danyjoe.lebanesemonateries.databinding.ActivitySettingsBinding
import com.danyjoe.lebanesemonateries.TranslationManager.setTranslatedText
import com.danyjoe.lebanesemonateries.TranslationManager.setTranslatedTitle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var userSettings: UserSettings? = null
    private var originalThemeMode: Int = UserSettings.THEME_SYSTEM

    private val activityScope = CoroutineScope(Dispatchers.Main + Job())
    private val TAG = "SettingsActivity"

    // List to hold language display names
    private lateinit var languageDisplayNames: List<String>
    // List to hold language codes corresponding to display names
    private lateinit var languageCodes: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Setup language spinner
        setupLanguageSpinner()

        // Set up UI listeners
        setupListeners()

        // Load user settings
        loadUserSettings()
    }

    private fun setupLanguageSpinner() {
        // Get language data
        languageCodes = TranslationManager.LANGUAGES.keys.toList()
        languageDisplayNames = TranslationManager.LANGUAGES.values.toList()

        // Create adapter with language display names
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            languageDisplayNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLanguage.adapter = adapter

        binding.spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Update language selection in userSettings
                userSettings = userSettings?.copy(language = languageCodes[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun setupListeners() {
        // Theme radio buttons
        binding.rgTheme.setOnCheckedChangeListener { _, checkedId ->
            val themeMode = when (checkedId) {
                R.id.rbLightTheme -> UserSettings.THEME_LIGHT
                R.id.rbDarkTheme -> UserSettings.THEME_DARK
                else -> UserSettings.THEME_SYSTEM
            }
            userSettings = userSettings?.copy(themeMode = themeMode)
        }

        // Notification switches
        binding.switchNotificationsEnabled.setOnCheckedChangeListener { _, isChecked ->
            userSettings = userSettings?.copy(notificationsEnabled = isChecked)
            updateNotificationSwitchesState(isChecked)
        }

        binding.switchNewMonasteries.setOnCheckedChangeListener { _, isChecked ->
            userSettings = userSettings?.copy(newMonasteriesNotifications = isChecked)
        }

        binding.switchUpdates.setOnCheckedChangeListener { _, isChecked ->
            userSettings = userSettings?.copy(appUpdatesNotifications = isChecked)
        }

        // Save button
        binding.btnSaveSettings.setOnClickListener {
            saveUserSettings()
        }

        // Delete account button
        binding.btnDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmationDialog()
        }
    }

    private fun loadUserSettings() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Not logged in, redirect to login
            Toast.makeText(this, TranslationManager.getString("please_login", this), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        activityScope.launch {
            try {
                // First try to get from Firestore
                val userSettingsDoc = db.collection("userSettings").document(currentUser.uid).get().await()

                userSettings = if (userSettingsDoc.exists()) {
                    UserSettings.fromMap(userSettingsDoc.data ?: emptyMap())
                } else {
                    // If no settings found, use default
                    val defaultSettings = UserSettings.defaultSettings()

                    // Save default settings to Firestore
                    try {
                        db.collection("userSettings").document(currentUser.uid)
                            .set(defaultSettings.toMap()).await()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error creating default settings: ${e.message}")
                    }

                    defaultSettings
                }

                // Store original theme mode for comparison later
                originalThemeMode = userSettings?.themeMode ?: UserSettings.THEME_SYSTEM

                // Update UI with settings
                updateUI()

            } catch (e: Exception) {
                Log.e(TAG, "Error loading settings: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SettingsActivity,
                        "Failed to load settings: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Use default settings on failure
                    userSettings = UserSettings.defaultSettings()
                    originalThemeMode = userSettings?.themeMode ?: UserSettings.THEME_SYSTEM
                    updateUI()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun updateUI() {
        userSettings?.let { settings ->
            // Update theme selection
            val radioButtonId = when (settings.themeMode) {
                UserSettings.THEME_LIGHT -> R.id.rbLightTheme
                UserSettings.THEME_DARK -> R.id.rbDarkTheme
                else -> R.id.rbSystemTheme
            }
            binding.rgTheme.check(radioButtonId)

            // Update notification switches
            binding.switchNotificationsEnabled.isChecked = settings.notificationsEnabled
            binding.switchNewMonasteries.isChecked = settings.newMonasteriesNotifications
            binding.switchUpdates.isChecked = settings.appUpdatesNotifications

            // Update dependencies
            updateNotificationSwitchesState(settings.notificationsEnabled)

            // Update language selection
            val languageIndex = languageCodes.indexOf(settings.language)
            if (languageIndex >= 0) {
                binding.spinnerLanguage.setSelection(languageIndex)
            } else {
                binding.spinnerLanguage.setSelection(0) // Default to English
            }
        }

        // Update all texts after UI is set up
        updateTexts()
    }

    override fun updateTexts() {
        // Update toolbar title
        binding.toolbar.setTranslatedTitle("settings", this)

        // Update section titles
        (binding.root.findViewWithTag("tvTheme") as? TextView)?.setTranslatedText("theme", this)
        (binding.root.findViewWithTag("tvNotifications") as? TextView)?.setTranslatedText("notifications", this)
        (binding.root.findViewWithTag("tvLanguage") as? TextView)?.setTranslatedText("language", this)
        (binding.root.findViewWithTag("tvAccount") as? TextView)?.setTranslatedText("account", this)

        // Update theme options
        binding.rbLightTheme.setTranslatedText("light_theme", this)
        binding.rbDarkTheme.setTranslatedText("dark_theme", this)
        binding.rbSystemTheme.setTranslatedText("system_theme", this)

        // Update notification options
        binding.switchNotificationsEnabled.setTranslatedText("enable_notifications", this)
        binding.switchNewMonasteries.setTranslatedText("new_monasteries", this)
        binding.switchUpdates.setTranslatedText("app_updates", this)

        // Update buttons
        binding.btnDeleteAccount.setTranslatedText("delete_account", this)
        binding.btnSaveSettings.setTranslatedText("save_settings", this)
    }

    private fun updateNotificationSwitchesState(enabled: Boolean) {
        binding.switchNewMonasteries.isEnabled = enabled
        binding.switchUpdates.isEnabled = enabled
    }

    private fun saveUserSettings() {
        val currentUser = auth.currentUser ?: return

        // Show loading
        binding.progressBar.visibility = View.VISIBLE

        // Check if language changed
        val oldLanguage = LocaleHelper.getLanguage(this)
        val newLanguage = userSettings?.language ?: oldLanguage
        val languageChanged = oldLanguage != newLanguage

        // Check if theme changed
        val newThemeMode = userSettings?.themeMode ?: UserSettings.THEME_SYSTEM
        val themeChanged = originalThemeMode != newThemeMode

        activityScope.launch {
            try {
                // Save to Firestore
                userSettings?.let { settings ->
                    db.collection("userSettings").document(currentUser.uid)
                        .set(settings.toMap()).await()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@SettingsActivity,
                            TranslationManager.getString("settings_saved", this@SettingsActivity),
                            Toast.LENGTH_SHORT
                        ).show()

                        // Apply settings
                        applySettings(settings, languageChanged, themeChanged)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving settings: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SettingsActivity,
                        "Failed to save settings: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun applySettings(settings: UserSettings, languageChanged: Boolean, themeChanged: Boolean) {
        // Apply theme using the application method
        MonasteriesApp.applyTheme(settings.themeMode)

        // Apply language if changed
        if (languageChanged) {
            updateLanguage(settings.language)
        } else if (themeChanged) {
            // If only theme changed, recreate the activity to apply changes
            binding.progressBar.visibility = View.GONE
            recreate()
        } else {
            // If neither changed, just hide progress
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun updateLanguage(languageCode: String) {
        // Save the language preference
        LocaleHelper.setLanguage(this, languageCode)

        // Apply to current activity
        updateTexts()

        recreate()

        // Show confirmation
        Toast.makeText(
            this,
            TranslationManager.getString("language_updated", this),
            Toast.LENGTH_SHORT
        ).show()

        // Restart the app to fully apply language changes

    }

    private fun restartApp() {
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finishAffinity() // Close all activities
    }

    private fun showDeleteAccountConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(TranslationManager.getString("delete_account", this))
            .setMessage(TranslationManager.getString("delete_account_confirm", this))
            .setPositiveButton(TranslationManager.getString("delete", this)) { _, _ ->
                deleteUserAccount()
            }
            .setNegativeButton(TranslationManager.getString("cancel", this), null)
            .show()
    }

    private fun deleteUserAccount() {
        val currentUser = auth.currentUser ?: return

        binding.progressBar.visibility = View.VISIBLE

        activityScope.launch {
            try {
                // Delete user data from Firestore
                val batch = db.batch()

                // Delete user document
                val userDoc = db.collection("users").document(currentUser.uid)
                batch.delete(userDoc)

                // Delete user settings
                val settingsDoc = db.collection("userSettings").document(currentUser.uid)
                batch.delete(settingsDoc)

                // Delete search history for this user
                try {
                    val searchHistoryDocs = db.collection("searchHistory")
                        .whereEqualTo("userId", currentUser.uid)
                        .get().await()

                    for (doc in searchHistoryDocs.documents) {
                        batch.delete(doc.reference)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error deleting search history: ${e.message}")
                    // Continue with account deletion even if search history deletion fails
                }

                // Commit the batch
                batch.commit().await()

                // Delete Firebase Auth account
                currentUser.delete().await()

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SettingsActivity,
                        TranslationManager.getString("account_deleted", this@SettingsActivity),
                        Toast.LENGTH_SHORT
                    ).show()

                    // Redirect to login
                    val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting account: ${e.message}")
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@SettingsActivity,
                        "Failed to delete account: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        activityScope.cancel()
    }
}