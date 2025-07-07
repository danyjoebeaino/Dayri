package com.danyjoe.lebanesemonateries

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

abstract class BaseActivity : BaseThemeActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Apply the saved language to the context
        super.attachBaseContext(LocaleHelper.setLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set layout direction for RTL languages like Arabic
        if (LocaleHelper.getLanguage(this) == "ar") {
            window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        } else {
            window.decorView.layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
    }

    override fun onResume() {
        super.onResume()
        // Update UI texts when activity resumes
        updateTexts()
    }

    // Must be implemented by each activity to update its UI texts
    abstract fun updateTexts()

    // Feedback dialog functionality
    fun showFeedbackDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_feedback, null)

        // Get references to views
        val etFeedback = dialogView.findViewById<TextInputEditText>(R.id.etFeedback)
        val tvFeedbackTitle = dialogView.findViewById<TextView>(R.id.tvFeedbackTitle)

        // Apply translations
        tvFeedbackTitle.text = TranslationManager.getString("feedback", this)

        // Create the dialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton(TranslationManager.getString("submit", this), null) // Set to null initially to prevent auto-dismiss
            .setNegativeButton(TranslationManager.getString("cancel", this)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        // Show the dialog
        dialog.show()

        // Override the positive button click to validate before dismissing
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val feedbackText = etFeedback.text.toString().trim()

            if (feedbackText.isEmpty()) {
                etFeedback.error = TranslationManager.getString("feedback_empty", this)
                return@setOnClickListener
            }

            // Submit feedback
            submitFeedback(feedbackText)

            // Dismiss the dialog
            dialog.dismiss()
        }
    }

    // Method to submit feedback to Firebase
    private fun submitFeedback(feedbackText: String) {
        val db = FirebaseFirestore.getInstance()
        val feedback = hashMapOf(
            "userId" to (FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"),
            "userEmail" to (FirebaseAuth.getInstance().currentUser?.email ?: "anonymous"),
            "feedback" to feedbackText,
            "timestamp" to System.currentTimeMillis(),
            "appVersion" to packageManager.getPackageInfo(packageName, 0).versionName,
            "deviceInfo" to "${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE}"
        )

        // Show loading state - can be a progress bar or toast
        Toast.makeText(this, TranslationManager.getString("please_wait", this), Toast.LENGTH_SHORT).show()

        db.collection("feedback")
            .add(feedback)
            .addOnSuccessListener {
                Toast.makeText(this, TranslationManager.getString("feedback_submitted", this), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "${TranslationManager.getString("feedback_error", this)}: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}