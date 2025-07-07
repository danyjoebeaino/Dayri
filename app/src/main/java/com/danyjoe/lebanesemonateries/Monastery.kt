package com.danyjoe.lebanesemonateries

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class Monastery(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val description: String = "",
    val history: String = "",
    val imageStoragePath: String = "",
    val lastUpdated: String = "",
    val yearFounded: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) : Parcelable {
    @IgnoredOnParcel
    val location: LatLng
        get() = LatLng(latitude, longitude)
}