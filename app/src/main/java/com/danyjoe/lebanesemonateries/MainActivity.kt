package com.danyjoe.lebanesemonateries

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.danyjoe.lebanesemonateries.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Show welcome message
        Toast.makeText(
            this,
            "Welcome to Lebanese Monasteries App!",
            Toast.LENGTH_LONG
        ).show()

        // Redirect to MapActivity - put this after toast if you want to see the toast
        startActivity(Intent(this, MapActivity::class.java))
        finish()

        // Note: This code will never be reached because of the finish() above
        // Setup logout button
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            finish()
        }
    }
}