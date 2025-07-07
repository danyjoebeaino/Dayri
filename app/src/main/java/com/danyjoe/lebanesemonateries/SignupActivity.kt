package com.danyjoe.lebanesemonateries
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.danyjoe.lebanesemonateries.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        setupListeners()
    }

    private fun setupListeners() {
        // Back button click listener
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher
        }

        // Login text click listener
        binding.tvLogin.setOnClickListener {
            finish() // Go back to login screen
        }

        // Birthday field click listener
        binding.etBirthday.setOnClickListener {
            showDatePicker()
        }

        // Signup button click listener
        binding.btnSignUp.setOnClickListener {
            registerUser()
        }
    }

    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                binding.etBirthday.setText(dateFormat.format(calendar.time))
            },
            year,
            month,
            day
        ).show()
    }

    private fun registerUser() {
        // Get user inputs
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val gender = when (binding.rgGender.checkedRadioButtonId) {
            R.id.rbMale -> "Male"
            R.id.rbFemale -> "Female"
            else -> ""
        }
        val birthday = binding.etBirthday.text.toString().trim()
        val phoneNumber =
            binding.ccp.selectedCountryCodeWithPlus + binding.etPhoneNumber.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        // Validate inputs
        if (fullName.isEmpty() || email.isEmpty() || gender.isEmpty() || birthday.isEmpty() ||
            phoneNumber.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()
        ) {
            Toast.makeText(this, getString(R.string.all_fields_required), Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, getString(R.string.passwords_dont_match), Toast.LENGTH_SHORT)
                .show()
            return
        }

        // Show progress bar
        binding.progressBar.visibility = View.VISIBLE

        // Create user with email and password
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign up success, save user data to Firestore
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    // Create user object to store in Firestore
                    val user = hashMapOf(
                        "fullName" to fullName,
                        "email" to email,
                        "gender" to gender,
                        "birthday" to birthday,
                        "phoneNumber" to phoneNumber,
                        "createdAt" to System.currentTimeMillis(),
                        "favoriteMonasteries" to emptyList<String>()
                    )

                    // Add user to Firestore
                    db.collection("users").document(userId)
                        .set(user)
                        .addOnSuccessListener {
                            // Hide progress bar
                            binding.progressBar.visibility = View.GONE

                            Toast.makeText(this, getString(R.string.signup_successful), Toast.LENGTH_SHORT).show()

                            // Navigate to EmailVerificationActivity
                            startActivity(Intent(this, EmailVerificationActivity::class.java))
                            finish()
                        }
                }
            }
    }

}