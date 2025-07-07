package com.danyjoe.lebanesemonateries

import android.content.Context
import com.google.android.gms.maps.model.LatLng

class TranslatableMonastery(private val monastery: Monastery, private val context: Context) {
    val id: String get() = monastery.id
    val latitude: Double get() = monastery.latitude
    val longitude: Double get() = monastery.longitude
    val imageStoragePath: String get() = monastery.imageStoragePath
    val lastUpdated: String get() = monastery.lastUpdated
    val yearFounded: Int get() = monastery.yearFounded
    val location: LatLng get() = monastery.location

    val name: String
        get() = TranslationManager.getMonasteryTranslation(
            monastery.id, "name", monastery.name, context
        )

    val description: String
        get() = TranslationManager.getMonasteryTranslation(
            monastery.id, "description", monastery.description, context
        )

    val history: String
        get() = TranslationManager.getMonasteryTranslation(
            monastery.id, "history", monastery.history, context
        )

    val address: String
        get() = TranslationManager.getMonasteryTranslation(
            monastery.id, "address", monastery.address, context
        )

    // Convert back to regular Monastery if needed
    fun toMonastery(): Monastery = monastery
}