// StorageUtils.kt
package com.danyjoe.lebanesemonateries

import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

object StorageUtils {
    private const val TAG = "StorageUtils"
    private val storage = FirebaseStorage.getInstance()

    // Get the download URL for an image in Firebase Storage
    suspend fun getDownloadUrl(imagePath: String): String? {
        return try {
            val storageRef = storage.reference.child(imagePath)
            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting download URL for $imagePath: ${e.message}")
            null
        }
    }
}