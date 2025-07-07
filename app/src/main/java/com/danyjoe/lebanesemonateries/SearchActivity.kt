package com.danyjoe.lebanesemonateries

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.danyjoe.lebanesemonateries.SearchHistoryAdapter
import com.danyjoe.lebanesemonateries.databinding.ActivitySearchBinding
import com.danyjoe.lebanesemonateries.utils.SearchHistoryManager
import com.google.firebase.firestore.FirebaseFirestore

//history
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var monasteryAdapter: MonasterySearchAdapter
    private lateinit var searchHistoryAdapter: SearchHistoryAdapter
    private lateinit var searchHistoryManager: SearchHistoryManager

    private val monasteryList = mutableListOf<Monastery>()
    private val filteredList = mutableListOf<Monastery>()
    private val searchHistoryList = mutableListOf<String>()

    private lateinit var db: FirebaseFirestore

    // Coroutine scope for Firestore operations
    private val activityScope = CoroutineScope(Dispatchers.Main + Job())

    private val TAG = "SearchActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Initialize Search History Manager
        searchHistoryManager = SearchHistoryManager(this)

        // Set up RecyclerView for monasteries
        setupRecyclerView()

        // Set up RecyclerView for search history
        setupSearchHistoryRecyclerView()

        // Set up search functionality
        setupSearch()

        // Load monasteries from Firestore
        loadMonasteries()

        // Load search history
        loadSearchHistory()



    }

    private fun setupRecyclerView() {
        monasteryAdapter = MonasterySearchAdapter(filteredList) { monastery ->
            // Handle monastery item click
            showMonasteryDetails(monastery)
        }

        binding.rvMonasteries.apply {
            adapter = monasteryAdapter
            layoutManager = LinearLayoutManager(this@SearchActivity)
        }
    }

    private fun setupSearchHistoryRecyclerView() {
        searchHistoryAdapter = SearchHistoryAdapter(
            searchHistoryList,
            onItemClick = { query ->
                // When a search history item is clicked, set it as the search text
                binding.etSearch.setText(query)
                binding.etSearch.setSelection(query.length)
                binding.llSearchHistory.visibility = View.GONE
                filterMonasteries(query)
            },
            onRemoveClick = { query ->
                // Remove search query from history - updated to use coroutines with async methods
                activityScope.launch {
                    searchHistoryManager.removeSearchQueryAsync(query)
                    loadSearchHistory()
                }
            }
        )

        binding.rvSearchHistory.apply {
            adapter = searchHistoryAdapter
            layoutManager = LinearLayoutManager(this@SearchActivity)
        }

        // Set up clear history button - updated to use coroutines with async methods
        binding.tvClearHistory.setOnClickListener {
            activityScope.launch {
                searchHistoryManager.clearSearchHistoryAsync()
                loadSearchHistory()
            }
        }
    }

    private fun setupSearch() {
        // Set up text change listener for search
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    // Show search history when search field is empty
                    binding.llSearchHistory.visibility = View.VISIBLE
                    loadSearchHistory()
                } else {
                    // Hide search history when typing
                    binding.llSearchHistory.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {
                filterMonasteries(s.toString())
            }
        })

        // Handle search action on keyboard - updated to use coroutines with async methods
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearch.text.toString()
                if (query.isNotEmpty()) {
                    // Save the search query to history using coroutines
                    activityScope.launch {
                        searchHistoryManager.addSearchQueryAsync(query)
                        loadSearchHistory()
                    }
                }
                return@setOnEditorActionListener true
            }
            false
        }

        // Set up focus listener for search
        binding.etSearch.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Show search history when search field is focused
                loadSearchHistory() // Always reload history data when gaining focus
                if (binding.etSearch.text.isNullOrEmpty()) {
                    binding.llSearchHistory.visibility = View.VISIBLE
                }
            }
        }

        binding.etSearch.setOnClickListener {
            if (binding.etSearch.text.isNullOrEmpty()) {
                loadSearchHistory()
                binding.llSearchHistory.visibility = View.VISIBLE
            }
        }


        // Set up filter chips
        binding.chipName.setOnCheckedChangeListener { _, _ -> filterMonasteries(binding.etSearch.text.toString()) }
        binding.chipLocation.setOnCheckedChangeListener { _, _ -> filterMonasteries(binding.etSearch.text.toString()) }
        binding.chipYearBefore1800.setOnCheckedChangeListener { _, _ -> filterMonasteries(binding.etSearch.text.toString()) }
        binding.chipYearAfter1800.setOnCheckedChangeListener { _, _ -> filterMonasteries(binding.etSearch.text.toString()) }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadSearchHistory() {
        // Updated to use coroutines with async methods
        activityScope.launch {
            val history = searchHistoryManager.getSearchHistoryAsync()
            searchHistoryList.clear()
            searchHistoryList.addAll(history)
            searchHistoryAdapter.notifyDataSetChanged()

            // Show/hide the search history section based on whether there are any items
            if (searchHistoryList.isEmpty()) {
                binding.llSearchHistory.visibility = View.GONE
            } else if (binding.etSearch.text.isNullOrEmpty() && binding.etSearch.hasFocus()) {
                binding.llSearchHistory.visibility = View.VISIBLE
            }
            // For debugging
            Log.d("SearchActivity", "Loaded ${searchHistoryList.size} history items: $searchHistoryList")
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadMonasteries() {
        binding.progressBar.visibility = View.VISIBLE

        db.collection("monasteries")
            .get()
            .addOnSuccessListener { result ->
                binding.progressBar.visibility = View.GONE
                monasteryList.clear()

                for (document in result) {
                    try {
                        val id = document.id
                        val name = document.getString("name") ?: ""
                        val address = document.getString("address") ?: ""
                        val description = document.getString("description") ?: ""
                        val history = document.getString("history") ?: ""
                        val imageStoragePath = document.getString("imageStoragePath") ?: ""
                        val lastUpdated = document.getString("lastUpdated") ?: ""
                        val yearFounded = document.getLong("yearFounded")?.toInt() ?: 0
                        val latitude = document.getDouble("latitude") ?: 0.0
                        val longitude = document.getDouble("longitude") ?: 0.0

                        val monastery = Monastery(
                            id = id,
                            name = name,
                            address = address,
                            description = description,
                            history = history,
                            imageStoragePath = imageStoragePath,
                            lastUpdated = lastUpdated,
                            yearFounded = yearFounded,
                            latitude = latitude,
                            longitude = longitude
                        )

                        monasteryList.add(monastery)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing monastery document: ${e.message}")
                    }
                }

                // Initialize filtered list with all monasteries
                filteredList.addAll(monasteryList)
                monasteryAdapter.notifyDataSetChanged()

                updateEmptyState()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Log.e(TAG, "Error getting monasteries: ", e)
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterMonasteries(query: String) {
        filteredList.clear()

        // Get filter states
        val filterByName = binding.chipName.isChecked
        val filterByLocation = binding.chipLocation.isChecked
        val filterBefore1800 = binding.chipYearBefore1800.isChecked
        val filterAfter1800 = binding.chipYearAfter1800.isChecked

        // If no year filter is selected, show all years
        val showAllYears = !filterBefore1800 && !filterAfter1800

        for (monastery in monasteryList) {
            // First check year filters
            val yearMatches = showAllYears ||
                    (filterBefore1800 && monastery.yearFounded < 1800) ||
                    (filterAfter1800 && monastery.yearFounded >= 1800)

            if (!yearMatches) continue

            // Then check text query if not empty
            if (query.isNotEmpty()) {
                val nameMatches = filterByName && monastery.name.contains(query, ignoreCase = true)
                val locationMatches = filterByLocation && monastery.address.contains(query, ignoreCase = true)

                if (nameMatches || locationMatches) {
                    filteredList.add(monastery)
                }
            } else {
                // If query is empty, add all monasteries that match year filter
                filteredList.add(monastery)
            }
        }

        monasteryAdapter.notifyDataSetChanged()
        updateEmptyState()
    }

    private fun updateEmptyState() {
        if (filteredList.isEmpty()) {
            binding.rvMonasteries.visibility = View.GONE
            binding.tvEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvMonasteries.visibility = View.VISIBLE
            binding.tvEmptyState.visibility = View.GONE
        }
    }

    private fun showMonasteryDetails(monastery: Monastery) {
        // Same implementation as in MapActivity
        val bottomSheet = MonasteryDetailsBottomSheet.newInstance(monastery)
        bottomSheet.show(supportFragmentManager, "MonasteryDetails")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher // Fixed to actually call the method
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // Cancel coroutines when activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        activityScope.cancel()
    }
}