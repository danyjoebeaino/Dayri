// ForgotPasswordActivity.kt
package com.danyjoe.lebanesemonateries

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.danyjoe.lebanesemonateries.TranslationManager.setTranslatedHint
import com.danyjoe.lebanesemonateries.TranslationManager.setTranslatedText
import com.danyjoe.lebanesemonateries.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : BaseActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        setupListeners()
        updateTexts()
    }

    override fun updateTexts() {
        // Update all UI elements with translated text
        binding.tvTitle.setTranslatedText("forgot_password", this)
        binding.tvDescription.setTranslatedText("forgot_password_description", this)
        binding.tilEmail.setTranslatedHint("email", this)
        binding.btnResetPassword.setTranslatedText("reset_password", this)
    }

    private fun setupListeners() {
        // Back button click listener
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Reset Password button click listener
        binding.btnResetPassword.setOnClickListener {
            resetPassword()
        }
    }

    private fun resetPassword() {
        val email = binding.etEmail.text.toString().trim()

        // Validate email
        if (email.isEmpty()) {
            Toast.makeText(this, TranslationManager.getString("email_required", this), Toast.LENGTH_SHORT).show()
            return
        }

        // Show progress bar
        binding.progressBar.visibility = View.VISIBLE

        // Send password reset email
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                // Hide progress bar
                binding.progressBar.visibility = View.GONE

                if (task.isSuccessful) {
                    // Email sent successfully
                    Toast.makeText(
                        this,
                        TranslationManager.getString("reset_email_sent", this),
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                } else {
                    // Failed to send email
                    Toast.makeText(
                        this,
                        "${TranslationManager.getString("reset_email_failed", this)}: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}