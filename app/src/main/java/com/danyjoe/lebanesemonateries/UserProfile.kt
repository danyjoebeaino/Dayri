// UserProfile.kt
package com.danyjoe.lebanesemonateries

data class UserProfile(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val gender: String = "",
    val birthday: String = "",
    val phoneNumber: String = "",
    val createdAt: Long = 0,
    val favoriteMonasteries: List<String> = emptyList() // Changed from favoriteTypes to favoriteMonasteries
) {
    companion object {
        fun fromMap(uid: String, data: Map<String, Any>): UserProfile {
            return UserProfile(
                uid = uid,
                fullName = data["fullName"] as? String ?: "",
                email = data["email"] as? String ?: "",
                gender = data["gender"] as? String ?: "",
                birthday = data["birthday"] as? String ?: "",
                phoneNumber = data["phoneNumber"] as? String ?: "",
                createdAt = data["createdAt"] as? Long ?: 0,
                favoriteMonasteries = (data["favoriteMonasteries"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            )
        }
    }

    fun toMap(): Map<String, Any> {
        val map = hashMapOf(
            "fullName" to fullName,
            "email" to email,
            "gender" to gender,
            "birthday" to birthday,
            "phoneNumber" to phoneNumber
        )

        if (favoriteMonasteries.isNotEmpty()) {
            map["favoriteMonasteries"] = favoriteMonasteries.toString()
        }

        return map
    }
}