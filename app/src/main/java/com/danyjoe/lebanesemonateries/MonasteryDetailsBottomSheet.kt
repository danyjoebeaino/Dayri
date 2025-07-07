package com.danyjoe.lebanesemonateries

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.danyjoe.lebanesemonateries.databinding.BottomSheetMonasteryDetailsBinding
import com.danyjoe.lebanesemonateries.TranslationManager.setTranslatedText
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class MonasteryDetailsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetMonasteryDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var monastery: Monastery
    private lateinit var translatableMonastery: TranslatableMonastery
    private var isFavorite = false

    companion object {
        private const val ARG_MONASTERY = "monastery"
        private const val TAG = "MonasteryDetails"

        fun newInstance(monastery: Monastery): MonasteryDetailsBottomSheet {
            val fragment = MonasteryDetailsBottomSheet()
            val args = Bundle()
            args.putParcelable(ARG_MONASTERY, monastery)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = BottomSheetMonasteryDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get monastery from arguments
        @Suppress("DEPRECATION")
        monastery = arguments?.getParcelable(ARG_MONASTERY)
            ?: throw IllegalArgumentException("Monastery details required")

        // Create a translatable wrapper
        translatableMonastery = TranslatableMonastery(monastery, requireContext())

        // Populate the views with monastery data
        setupViews()
        setupButtons()

        // Check if monastery is already in favorites
        checkIfFavorite()
    }

    @SuppressLint("SetTextI18n")
    private fun setupViews() {
        // Set monastery name
        binding.tvMonasteryName.text = translatableMonastery.name

        // Set location/address
        binding.tvLocation.text = translatableMonastery.address

        // Set description
        binding.tvDescription.text = translatableMonastery.description

        // Set history
        binding.tvHistory.text = translatableMonastery.history

        // Set year founded
        binding.tvYearFounded.text = monastery.yearFounded.toString()

        // Set last updated
        binding.tvLastUpdated.text = "Last updated: ${monastery.lastUpdated}"

        // Translate section labels
        binding.tvDescriptionLabel.setTranslatedText("description", requireContext())
        binding.tvHistoryLabel.setTranslatedText("history", requireContext())
        binding.tvYearFoundedLabel.setTranslatedText("year_founded", requireContext())

        // Load the monastery image from Firebase Storage
        loadImageFromStorage(monastery.imageStoragePath)
    }

    private fun loadImageFromStorage(storagePath: String) {
        // Show placeholder while loading
        binding.ivMonastery.setImageResource(R.drawable.placeholder_monastery)

        Log.d(TAG, "Attempting to load image from path: $storagePath")

        if (storagePath.isEmpty()) {
            Log.e(TAG, "Storage path is empty, using placeholder")
            return
        }

        // Get the download URL from Firebase Storage
        val storageRef = FirebaseStorage.getInstance().reference.child(storagePath)
        Log.d(TAG, "Created storage reference: ${storageRef.path}")

        storageRef.downloadUrl
            .addOnSuccessListener { uri ->
                Log.d(TAG, "Successfully got download URL: $uri")
                // Load the image with Glide
                Glide.with(requireContext())
                    .load(uri)
                    .placeholder(R.drawable.placeholder_monastery)
                    .error(R.drawable.placeholder_monastery)
                    .into(binding.ivMonastery)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get download URL: ${e.message}")
            }
    }

    private fun setupButtons() {
        // Set up directions button
        binding.btnDirections.setOnClickListener {
            openGoogleMapsDirections(monastery.location.latitude, monastery.location.longitude)
        }

        // Set up share button
        binding.btnShare.setOnClickListener {
            shareMonasteryInfo()
        }

        // Set up favorite button
        binding.btnFavorite.setOnClickListener {
            toggleFavorite()
        }

        // Translate button text
        binding.btnDirections.setTranslatedText("get_directions", requireContext())
        binding.btnShare.setTranslatedText("share", requireContext())
    }

    // Rest of the existing code for checkIfFavorite, updateFavoriteButton, toggleFavorite, etc.
    private fun checkIfFavorite() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val favoriteMonasteries = document.get("favoriteMonasteries") as? List<*> ?: emptyList<String>()
                    isFavorite = favoriteMonasteries.contains(monastery.id)

                    // Update button appearance based on favorite status
                    updateFavoriteButton(isFavorite)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error checking favorite status: ${e.message}")
            }
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        this.isFavorite = isFavorite
        if (isFavorite) {
            binding.btnFavorite.setTranslatedText("remove_from_favorites", requireContext())
            binding.btnFavorite.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_filled, 0, 0, 0)
        } else {
            binding.btnFavorite.setTranslatedText("add_to_favorites", requireContext())
            binding.btnFavorite.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_outline, 0, 0, 0)
        }
    }

    private fun toggleFavorite() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Please log in to add favorites", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(currentUser.uid)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)

            // Initialize the list safely
            val favoriteMonasteriesList = when (val favoriteMonasteries = snapshot.get("favoriteMonasteries")) {
                is List<*> -> favoriteMonasteries.filterIsInstance<String>().toMutableList()
                null -> mutableListOf()
                else -> mutableListOf()
            }

            val isFavorited = favoriteMonasteriesList.contains(monastery.id)
            if (isFavorited) {
                // Remove from favorites
                favoriteMonasteriesList.remove(monastery.id)
            } else {
                // Add to favorites
                favoriteMonasteriesList.add(monastery.id)
            }

            transaction.update(userRef, "favoriteMonasteries", favoriteMonasteriesList)

            // Return whether it's now favorited or not
            !isFavorited
        }.addOnSuccessListener { isFavorited ->
            // Update UI on the main thread
            activity?.runOnUiThread {
                updateFavoriteButton(isFavorited)
                val message = if (isFavorited) {
                    TranslationManager.getString("added_to_favorites", requireContext())
                } else {
                    TranslationManager.getString("removed_from_favorites", requireContext())
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error updating favorites: ${e.message}")
            activity?.runOnUiThread {
                Toast.makeText(context, "Error updating favorites: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGoogleMapsDirections(latitude: Double, longitude: Double) {
        val uri = "google.navigation:q=$latitude,$longitude&mode=d".toUri()
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(mapIntent)
        } else {
            // If Google Maps is not installed, open in browser
            val browserUri =
                "https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude".toUri()
            val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
            startActivity(browserIntent)
        }
    }

    private fun shareMonasteryInfo() {
        val shareText = """
            ${TranslationManager.getString("check_out", requireContext())} ${translatableMonastery.name}!
            
            ${translatableMonastery.description}
            
            ${TranslationManager.getString("located_at", requireContext())}: ${translatableMonastery.address}
            ${TranslationManager.getString("founded_in", requireContext())}: ${monastery.yearFounded}
            
            ${TranslationManager.getString("learn_more_app", requireContext())}
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, TranslationManager.getString("share_monastery", requireContext()))
        startActivity(shareIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}