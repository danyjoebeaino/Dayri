package com.danyjoe.lebanesemonateries

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.danyjoe.lebanesemonateries.FavoriteMonasteriesAdapter
import com.danyjoe.lebanesemonateries.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var userProfile: UserProfile? = null
    private var isEditMode = false
    private val monasteryNames = mutableMapOf<String, String>()

    private val activityScope = CoroutineScope(Dispatchers.Main + Job())
    private val TAG = "ProfileActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Profile"

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Set up UI
        setupUI()

        // Load user profile
        loadUserProfile()
    }

    private fun setupUI() {
        // Set up edit button
        binding.btnEdit.setOnClickListener {
            toggleEditMode()
        }

        // Set up save button
        binding.btnSave.setOnClickListener {
            if (validateInputs()) {
                saveUserProfile()
            }
        }
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Not logged in, redirect to login
            Toast.makeText(this, "Please login to view profile", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        activityScope.launch {
            try {
                // Get user document from Firestore
                val userDoc = db.collection("users").document(currentUser.uid).get().await()

                if (userDoc.exists()) {
                    // User exists in Firestore
                    userProfile = UserProfile.fromMap(currentUser.uid, userDoc.data ?: emptyMap())
                } else {
                    // Create new user profile if it doesn't exist
                    userProfile = UserProfile(
                        uid = currentUser.uid,
                        fullName = currentUser.displayName ?: "",
                        email = currentUser.email ?: "",
                        createdAt = System.currentTimeMillis()
                    )

                    // Save new profile to Firestore
                    db.collection("users").document(currentUser.uid)
                        .set(userProfile!!.toMap()).await()
                }

                // Update UI with profile data
                updateUIWithProfile()

            } catch (e: Exception) {
                Log.e(TAG, "Error loading profile: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Failed to load profile: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun updateUIWithProfile() {
        userProfile?.let { profile ->
            binding.etFullName.setText(profile.fullName)
            binding.tvEmail.text = profile.email
            binding.tvGender.text = profile.gender
            binding.tvBirthday.text = profile.birthday
            binding.tvPhoneNumber.text = profile.phoneNumber

            // Update favorites list
            if (profile.favoriteMonasteries.isEmpty()) {
                binding.tvNoFavorites.visibility = View.VISIBLE
                binding.rvFavorites.visibility = View.GONE
            } else {
                binding.tvNoFavorites.visibility = View.GONE
                binding.rvFavorites.visibility = View.VISIBLE

                // Load monastery names for the favorites
                loadMonasteryNames()
            }
        }

        // Set initial edit mode
        setEditMode(false)
    }

    private fun setupRecyclerView() {
        val favoritesList = userProfile?.favoriteMonasteries ?: emptyList()

        val adapter = FavoriteMonasteriesAdapter(
            favoritesList,
            monasteryNames,
            onItemClick = { monasteryId ->
                // Navigate to monastery details
                showMonasteryDetails(monasteryId)
            },
            onRemoveClick = { monasteryId ->
                // Remove monastery from favorites
                removeFavoriteMonastery(monasteryId)
            }
        )

        binding.rvFavorites.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(this@ProfileActivity)
        }
    }

    private fun loadMonasteryNames() {
        activityScope.launch {
            try {
                val snapshot = db.collection("monasteries").get().await()
                for (document in snapshot.documents) {
                    val id = document.id
                    val name = document.getString("name") ?: "Unknown"
                    monasteryNames[id] = name
                }

                // Setup recycler view now that we have the names
                setupRecyclerView()

            } catch (e: Exception) {
                Log.e(TAG, "Error loading monastery names: ${e.message}")
            }
        }
    }

    private fun showMonasteryDetails(monasteryId: String) {
        // Query the monastery from Firestore
        activityScope.launch {
            try {
                val document = db.collection("monasteries").document(monasteryId).get().await()
                if (document.exists()) {
                    // Convert to Monastery object
                    val monastery = Monastery(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        address = document.getString("address") ?: "",
                        description = document.getString("description") ?: "",
                        history = document.getString("history") ?: "",
                        imageStoragePath = document.getString("imageStoragePath") ?: "",
                        lastUpdated = document.getString("lastUpdated") ?: "",
                        yearFounded = document.getLong("yearFounded")?.toInt() ?: 0,
                        latitude = document.getDouble("latitude") ?: 0.0,
                        longitude = document.getDouble("longitude") ?: 0.0
                    )

                    // Show monastery details
                    val bottomSheet = MonasteryDetailsBottomSheet.newInstance(monastery)
                    bottomSheet.show(supportFragmentManager, "MonasteryDetails")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting monastery details: ${e.message}")
                Toast.makeText(this@ProfileActivity, "Error loading monastery details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun removeFavoriteMonastery(monasteryId: String) {
        // Get current user
        val currentUser = auth.currentUser ?: return

        // Show loading
        binding.progressBar.visibility = View.VISIBLE

        // Update the user profile
        val updatedFavorites = userProfile?.favoriteMonasteries?.toMutableList() ?: mutableListOf()
        updatedFavorites.remove(monasteryId)

        userProfile = userProfile?.copy(
            favoriteMonasteries = updatedFavorites
        )

        activityScope.launch {
            try {
                // Update in Firestore
                db.collection("users").document(currentUser.uid)
                    .update("favoriteMonasteries", updatedFavorites).await()

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProfileActivity, "Removed from favorites", Toast.LENGTH_SHORT).show()

                    // Update UI
                    updateUIWithProfile()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error removing favorite: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProfileActivity, "Error removing favorite", Toast.LENGTH_SHORT).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun toggleEditMode() {
        setEditMode(!isEditMode)
    }

    private fun setEditMode(editMode: Boolean) {
        isEditMode = editMode

        // Enable/disable editing
        binding.etFullName.isEnabled = editMode

        // Show/hide buttons
        binding.btnEdit.visibility = if (editMode) View.GONE else View.VISIBLE
        binding.btnSave.visibility = if (editMode) View.VISIBLE else View.GONE
    }

    private fun validateInputs(): Boolean {
        val fullName = binding.etFullName.text.toString().trim()

        if (fullName.isEmpty()) {
            binding.etFullName.error = "Name cannot be empty"
            return false
        }

        return true
    }

    private fun saveUserProfile() {
        val currentUser = auth.currentUser ?: return

        // Show loading
        binding.progressBar.visibility = View.VISIBLE

        // Get updated values
        val fullName = binding.etFullName.text.toString().trim()

        // Update profile object
        userProfile = userProfile?.copy(
            fullName = fullName
        )

        activityScope.launch {
            try {
                // Save to Firestore
                userProfile?.let { profile ->
                    // Only update the fullName field
                    db.collection("users").document(currentUser.uid)
                        .update("fullName", profile.fullName).await()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ProfileActivity,
                            "Profile updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Exit edit mode
                        setEditMode(false)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving profile: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Failed to save profile: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        (activityScope.coroutineContext[Job] as? Job)?.cancel()
    }
}