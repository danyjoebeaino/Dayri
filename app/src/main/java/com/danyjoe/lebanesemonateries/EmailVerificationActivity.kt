// EmailVerificationActivity.kt
package com.danyjoe.lebanesemonateries

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.danyjoe.lebanesemonateries.databinding.ActivityEmailVerificationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class EmailVerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmailVerificationBinding
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private var countDownTimer: CountDownTimer? = null
    private val resendTimeInSeconds = 60L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        // Get email from intent or current user
        val email = currentUser?.email ?: "your email"
        binding.tvEmailAddress.text = email

        setupListeners()
        sendVerificationEmail()
    }

    private fun setupListeners() {
        binding.btnVerify.setOnClickListener {
            // Refresh user to check verification status
            currentUser?.reload()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    currentUser = auth.currentUser
                    if (currentUser?.isEmailVerified == true) {
                        // Email is verified, proceed to main activity
                        Toast.makeText(this, "Email verified successfully!", Toast.LENGTH_SHORT).show()
                        startMainActivity()
                    } else {
                        Toast.makeText(this, "Email not verified yet. Please check your inbox.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.tvResendCode.setOnClickListener {
            if (binding.tvResendCode.isEnabled) {
                sendVerificationEmail()
            }
        }
    }

    private fun sendVerificationEmail() {
        binding.progressBar.visibility = View.VISIBLE

        currentUser?.sendEmailVerification()?.addOnCompleteListener { task ->
            binding.progressBar.visibility = View.GONE

            if (task.isSuccessful) {
                Toast.makeText(this, "Verification email sent to ${currentUser?.email}", Toast.LENGTH_SHORT).show()
                startResendTimer()
            } else {
                Toast.makeText(this, "Failed to send verification email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startResendTimer() {
        binding.tvResendCode.isEnabled = false

        countDownTimer = object : CountDownTimer(resendTimeInSeconds * 1000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                binding.tvTimer.text = "Resend code in ${millisUntilFinished / 1000} seconds"
                binding.tvTimer.visibility = View.VISIBLE
            }

            override fun onFinish() {
                binding.tvResendCode.isEnabled = true
                binding.tvTimer.visibility = View.GONE
            }
        }.start()
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}