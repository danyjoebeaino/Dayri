package com.danyjoe.lebanesemonateries

data class UserSettings(
    val themeMode: Int = THEME_SYSTEM, // 0: System, 1: Light, 2: Dark
    val notificationsEnabled: Boolean = true,
    val newMonasteriesNotifications: Boolean = true,
    val appUpdatesNotifications: Boolean = true,
    val language: String = "en" // en, fr, ar
) {
    companion object {
        const val THEME_SYSTEM = -1
        const val THEME_LIGHT = 1
        const val THEME_DARK = 2

        fun fromMap(data: Map<String, Any>): UserSettings {
            return UserSettings(
                themeMode = (data["themeMode"] as? Long)?.toInt() ?: THEME_SYSTEM,
                notificationsEnabled = data["notificationsEnabled"] as? Boolean ?: true,
                newMonasteriesNotifications = data["newMonasteriesNotifications"] as? Boolean ?: true,
                appUpdatesNotifications = data["appUpdatesNotifications"] as? Boolean ?: true,
                language = data["language"] as? String ?: "en"
            )
        }

        fun defaultSettings(): UserSettings {
            return UserSettings()
        }
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            "themeMode" to themeMode,
            "notificationsEnabled" to notificationsEnabled,
            "newMonasteriesNotifications" to newMonasteriesNotifications,
            "appUpdatesNotifications" to appUpdatesNotifications,
            "language" to language
        )
    }
}