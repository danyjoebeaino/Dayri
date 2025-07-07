package com.danyjoe.lebanesemonateries.utils

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class SearchHistoryManager(context: Context) {

    companion object {
        private const val MAX_HISTORY_ITEMS = 5
        private const val TAG = "SearchHistoryManager"
    }

    // Firebase instances
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val searchHistoryCollection = db.collection("searchHistory")

    // Get the current user ID or a default value if not logged in
    private val currentUserId: String
        get() = auth.currentUser?.uid ?: "anonymous"

    // Get search history from Firestore
    suspend fun getSearchHistoryAsync(): List<String> {
        return try {
            val snapshot = searchHistoryCollection
                .whereEqualTo("userId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(MAX_HISTORY_ITEMS.toLong())
                .get()
                .await()

            val queries = snapshot.documents.mapNotNull { it.getString("query") }
            Log.d(TAG, "Retrieved ${queries.size} search history items")
            queries
        } catch (e: Exception) {
            Log.e(TAG, "Error getting search history: ${e.message}")
            emptyList()
        }
    }

    // Add a new search query to Firestore
    suspend fun addSearchQueryAsync(query: String) {
        if (query.isBlank()) return

        try {
            Log.d(TAG, "Adding search query: $query")

            // Check if this query already exists for this user
            val existingQuery = searchHistoryCollection
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("query", query)
                .get()
                .await()

            // If it exists, delete the old one (we'll add a new one with updated timestamp)
            if (!existingQuery.isEmpty) {
                val documentId = existingQuery.documents.first().id
                searchHistoryCollection.document(documentId).delete().await()
            }

            // Add the new search query
            val searchData = hashMapOf(
                "query" to query,
                "userId" to currentUserId,
                "timestamp" to com.google.firebase.Timestamp.now()
            )

            searchHistoryCollection.add(searchData).await()
            Log.d(TAG, "Search query added successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error adding search query: ${e.message}")
        }
    }

    // Clear all search history from Firestore
    suspend fun clearSearchHistoryAsync() {
        try {
            Log.d(TAG, "Clearing search history")

            val batch = db.batch()
            val querySnapshot = searchHistoryCollection
                .whereEqualTo("userId", currentUserId)
                .get()
                .await()

            // Add delete operations to batch
            for (document in querySnapshot.documents) {
                batch.delete(searchHistoryCollection.document(document.id))
            }

            // Execute the batch
            batch.commit().await()

            Log.d(TAG, "Search history cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing search history: ${e.message}")
        }
    }

    // Remove a specific search query from Firestore
    suspend fun removeSearchQueryAsync(query: String) {
        try {
            Log.d(TAG, "Removing search query: $query")

            val querySnapshot = searchHistoryCollection
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("query", query)
                .get()
                .await()

            for (document in querySnapshot.documents) {
                searchHistoryCollection.document(document.id).delete().await()
            }

            Log.d(TAG, "Search query removed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error removing search query: ${e.message}")
        }
    }

    // Non-suspend versions that call the suspend functions using callbacks

    fun getSearchHistory(): List<String> {
        // For backward compatibility, return empty list
        Log.w(TAG, "Using non-suspend getSearchHistory(). Consider using getSearchHistoryAsync() with coroutines.")
        return emptyList()
    }

    fun addSearchQuery(query: String) {
        Log.w(TAG, "Using non-suspend addSearchQuery(). Consider using addSearchQueryAsync() with coroutines.")
        // Do nothing in the non-suspend version
    }

    fun clearSearchHistory() {
        Log.w(TAG, "Using non-suspend clearSearchHistory(). Consider using clearSearchHistoryAsync() with coroutines.")
        // Do nothing in the non-suspend version
    }

    fun removeSearchQuery(query: String) {
        Log.w(TAG, "Using non-suspend removeSearchQuery(). Consider using removeSearchQueryAsync() with coroutines.")
        // Do nothing in the non-suspend version
    }
}