package com.danyjoe.lebanesemonateries

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.danyjoe.lebanesemonateries.databinding.ActivityLoginBinding
import com.danyjoe.lebanesemonateries.TranslationManager.setTranslatedText
import com.danyjoe.lebanesemonateries.TranslationManager.setTranslatedHint
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        setupListeners()
    }

    override fun updateTexts() {
        // Update all UI elements with translated text
        binding.tvAppName.setTranslatedText("app_name", this)
        binding.tvSubtitle.setTranslatedText("discover_lebanese_monasteries", this)

        // Input fields
        binding.tilEmail.setTranslatedHint("email", this)
        binding.tilPassword.setTranslatedHint("password", this)

        // Buttons and clickable text
        binding.btnLogin.setTranslatedText("login", this)
        binding.tvForgotPassword.setTranslatedText("forgot_password", this)
        binding.tvNoAccount.setTranslatedText("no_account_yet", this)
        binding.tvSignUp.setTranslatedText("sign_up", this)
    }

    private fun setupListeners() {
        // Login button click listener
        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        // Sign up text click listener
        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        // Forgot password text click listener
        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun loginUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Reset error states before validation
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        // Validate inputs
        if (email.isEmpty()) {
            binding.tilEmail.error = TranslationManager.getString("email_required", this)
            return
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = TranslationManager.getString("password_required", this)
            return
        }

        // Show progress bar
        binding.progressBar.visibility = View.VISIBLE

        // Authenticate with Firebase
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                // Hide progress bar
                binding.progressBar.visibility = View.GONE

                if (task.isSuccessful) {
                    // Check if email is verified
                    if (auth.currentUser?.isEmailVerified == true) {
                        // Email verified, go to MapActivity
                        startActivity(Intent(this, MapActivity::class.java))
                    } else {
                        // Email not verified, go to EmailVerificationActivity
                        startActivity(Intent(this, EmailVerificationActivity::class.java))
                    }
                    finish()
                } else {
                    // Login failed - Analyze the error
                    val errorMessage = task.exception?.message ?: ""

                    // Different handling based on error type
                    when {
                        // Check for "user not found" or email-related errors
                        errorMessage.contains("no user record", ignoreCase = true) ||
                                errorMessage.contains("user not found", ignoreCase = true) ||
                                errorMessage.contains("invalid email", ignoreCase = true) -> {
                            // Highlight email field with error
                            binding.tilEmail.error = TranslationManager.getString("user_not_found", this)
                        }
                        // Check for password-related errors
                        errorMessage.contains("password", ignoreCase = true) ||
                                errorMessage.contains("credential", ignoreCase = true) -> {
                            // Highlight the password field with error
                            binding.tilPassword.error = TranslationManager.getString("invalid_password", this)
                        }
                        // Generic error case
                        else -> {
                            Toast.makeText(
                                this,
                                "${TranslationManager.getString("login_failed", this)}: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
    }
}