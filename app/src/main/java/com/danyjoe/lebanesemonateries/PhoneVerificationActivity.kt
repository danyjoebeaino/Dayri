package com.danyjoe.lebanesemonateries

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.danyjoe.lebanesemonateries.databinding.ActivityPhoneVerificationBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.rpc.Help.Link
import java.util.concurrent.TimeUnit

class PhoneVerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhoneVerificationBinding
    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null
    private var phoneNumber: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var countDownTimer: CountDownTimer? = null
    private val resendTimeInSeconds = 60L

    // All OTP edit text fields
    private lateinit var otpEditTexts: Array<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Get phone number from intent
        phoneNumber = intent.getStringExtra("PHONE_NUMBER")

        // If phone number is not passed from signup, get from current user
        if (phoneNumber.isNullOrEmpty() && auth.currentUser != null) {
            // TODO: Get phone number from Firestore user document
            // For now, we'll use a dummy placeholder
            phoneNumber = "+961123456789"
        }

        // Display phone number
        binding.tvPhoneNumber.text = phoneNumber

        // Initialize OTP edit text fields array
        otpEditTexts = arrayOf(
            binding.etOtp1,
            binding.etOtp2,
            binding.etOtp3,
            binding.etOtp4,
            binding.etOtp5,
            binding.etOtp6
        )

        setupOtpInputs()
        setupListeners()

        // Start phone verification
        startPhoneNumberVerification(phoneNumber!!)
    }

    private fun setupOtpInputs() {
        // Set up auto-focus for OTP fields
        for (i in otpEditTexts.indices) {
            otpEditTexts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    // Auto-focus next field when current is filled
                    if (s?.length == 1 && i < otpEditTexts.size - 1) {
                        otpEditTexts[i + 1].requestFocus()
                    }

                    // Check if all fields are filled
                    binding.btnVerify.isEnabled = isOtpComplete()
                }
            })
        }
    }

    private fun isOtpComplete(): Boolean {
        return otpEditTexts.none { it.text.isNullOrEmpty() }
    }

    private fun getOtpFromEditTexts(): String {
        return otpEditTexts.joinToString("") { it.text.toString() }
    }

    private fun setupListeners() {
        // Back button click listener
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher
        }

        // Verify button click listener
        binding.btnVerify.setOnClickListener {
            val otp = getOtpFromEditTexts()
            if (otp.length == 6) {
                verifyPhoneNumberWithCode(verificationId, otp)
            } else {
                Toast.makeText(this, getString(R.string.enter_valid_code), Toast.LENGTH_SHORT).show()
            }
        }

        // Resend code click listener
        binding.tvResendCode.setOnClickListener {
            if (binding.tvResendCode.isEnabled) {
                resendVerificationCode(phoneNumber!!, resendToken)
            }
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        binding.progressBar.visibility = View.VISIBLE

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
        startResendTimer()
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        if (verificationId != null) {
            binding.progressBar.visibility = View.VISIBLE

            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            signInWithPhoneAuthCredential(credential)
        }
    }

    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?
    ) {
        binding.progressBar.visibility = View.VISIBLE

        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)

        if (token != null) {
            optionsBuilder.setForceResendingToken(token)
        }

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
        startResendTimer()
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                binding.progressBar.visibility = View.GONE

                if (task.isSuccessful) {
                    // Verification successful, navigate to main activity
                    Toast.makeText(this, getString(R.string.verification_successful), Toast.LENGTH_SHORT).show()

                    // Navigate to main activity
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    // Verification failed
                    val errorMessage = task.exception?.message ?: "Invalid verification code"
                    Toast.makeText(
                        this,
                        getString(R.string.error_occurred, errorMessage),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun startResendTimer() {
        binding.tvResendCode.isEnabled = false

        countDownTimer = object : CountDownTimer(resendTimeInSeconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvTimer.text = getString(R.string.resend_timer, millisUntilFinished / 1000)
                binding.tvTimer.visibility = View.VISIBLE
            }

            override fun onFinish() {
                binding.tvResendCode.isEnabled = true
                binding.tvTimer.visibility = View.GONE
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    // Callbacks for Phone Auth
    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            binding.progressBar.visibility = View.GONE
            signInWithPhoneAuthCredential(credential)

            // Auto-fill the OTP fields if code is retrieved
            val code = credential.smsCode
            if (code != null) {
                for (i in code.indices) {
                    if (i < otpEditTexts.size) {
                        otpEditTexts[i].setText(code[i].toString())
                    }
                }
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked if an invalid request for verification is made,
            // for instance if the phone number format is not valid.
            binding.progressBar.visibility = View.GONE
            Toast.makeText(
                this@PhoneVerificationActivity,
                getString(R.string.error_occurred, e.message ?: "Verification failed"),
                Toast.LENGTH_LONG
            ).show()
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number
            // Now we need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this@PhoneVerificationActivity, getString(R.string.code_sent), Toast.LENGTH_SHORT).show()

            // Save verification ID and resending token for later use
            this@PhoneVerificationActivity.verificationId = verificationId
            resendToken = token
        }

        override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
            // Called when the timeout duration has passed without auto-retrieval
            // Enable the resend button
            binding.tvResendCode.isEnabled = true
            binding.tvTimer.visibility = View.GONE
        }
    }
}