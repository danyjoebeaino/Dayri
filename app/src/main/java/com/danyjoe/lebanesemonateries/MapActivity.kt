// MapActivity.kt
package com.danyjoe.lebanesemonateries

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.danyjoe.lebanesemonateries.databinding.ActivityMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MapActivity : BaseActivity(), OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMapBinding
    private lateinit var mMap: GoogleMap
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    private val monasteryList = mutableListOf<Monastery>()
    private val TAG = "MapActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Set up toolbar - Make sure to set it as support action bar
        setSupportActionBar(binding.toolbar)

        // Set up drawer layout
        drawerLayout = binding.drawerLayout
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Handle RTL layout for drawer
        if (LocaleHelper.getLanguage(this) == "ar") {
            binding.drawerLayout.layoutDirection = View.LAYOUT_DIRECTION_RTL
        } else {
            binding.drawerLayout.layoutDirection = View.LAYOUT_DIRECTION_LTR
        }

        // Set up navigation view
        binding.navView.setNavigationItemSelectedListener(this)

        // Set up loading indicator
        binding.progressBar.visibility = View.VISIBLE

        // Obtain the SupportMapFragment and get notified when the map is ready to be used
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Update drawer text
        updateTexts()
    }

    override fun updateTexts() {
        // Update toolbar title
        supportActionBar?.title = TranslationManager.getString("app_name", this)

        // Update navigation drawer menu items
        val menu = binding.navView.menu
        menu.findItem(R.id.nav_home).title = TranslationManager.getString("home", this)
        menu.findItem(R.id.nav_search).title = TranslationManager.getString("search", this)
        menu.findItem(R.id.nav_profile).title = TranslationManager.getString("profile", this)
        menu.findItem(R.id.nav_settings).title = TranslationManager.getString("settings", this)
        menu.findItem(R.id.nav_logout).title = TranslationManager.getString("logout", this)

        // Update header text if you have any in your navigation drawer header
        val headerView = binding.navView.getHeaderView(0)
        val tvSubtitle = headerView.findViewById<TextView>(R.id.textView)
        tvSubtitle?.text = TranslationManager.getString("discover_lebanese_cultural_heritage", this)
    }

    @Deprecated("Use onBackPressedDispatcher instead")
    override fun onBackPressed() {
        super.onBackPressedDispatcher
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                Toast.makeText(this, TranslationManager.getString("home", this), Toast.LENGTH_SHORT).show()
            }
            R.id.nav_search -> {
                startActivity(Intent(this, SearchActivity::class.java))
            }
            R.id.nav_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
            }
            R.id.nav_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.nav_feedback -> {
                showFeedbackDialog()
            }
            R.id.nav_logout -> {
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Set default location to Lebanon
        val lebanon = LatLng(33.8547, 35.8623)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lebanon, 8f))

        // Load monasteries from Firestore
        loadMonasteriesFromFirestore()
    }

    private fun loadMonasteriesFromFirestore() {
        binding.progressBar.visibility = View.VISIBLE

        db.collection("monasteries")
            .get()
            .addOnSuccessListener { result ->
                binding.progressBar.visibility = View.GONE
                monasteryList.clear()

                for (document in result) {
                    try {
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

                        monasteryList.add(monastery)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing monastery: ${e.message}")
                    }
                }

                // Add markers for monasteries
                addMonasteryMarkers()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Error loading monasteries: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error loading monasteries: ${e.message}")
            }
    }

    private fun addMonasteryMarkers() {
        for (monastery in monasteryList) {
            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(monastery.location)
                    .title(monastery.name)
            )

            // Store monastery ID as tag for later use
            marker?.tag = monastery.id
        }

        // Set marker click listener
        mMap.setOnMarkerClickListener { marker ->
            val monasteryId = marker.tag as? String
            if (monasteryId != null) {
                val monastery = monasteryList.find { it.id == monasteryId }
                monastery?.let {
                    showMonasteryDetails(it)
                }
            }
            false // Return false to allow default behavior (info window)
        }
    }

    private fun showMonasteryDetails(monastery: Monastery) {
        val bottomSheet = MonasteryDetailsBottomSheet.newInstance(monastery)
        bottomSheet.show(supportFragmentManager, "MonasteryDetails")
    }
}