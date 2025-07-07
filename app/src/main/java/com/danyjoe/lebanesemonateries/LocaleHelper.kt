package com.danyjoe.lebanesemonateries

import android.content.Context
import android.content.res.Configuration
import java.util.Locale
import androidx.core.content.edit

object LocaleHelper {
    const val PREF_NAME = "AppPrefs"
    const val PREF_LANGUAGE_KEY = "app_language"

    fun setLocale(context: Context): Context {
        val language = getLanguage(context)
        return updateResources(context, language)
    }

    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(PREF_LANGUAGE_KEY, "en") ?: "en"
    }

    fun setLanguage(context: Context, language: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit() { putString(PREF_LANGUAGE_KEY, language) }
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)

        // Set layout direction explicitly based on locale
        if (language == "ar") {
            configuration.setLayoutDirection(Locale("ar"))
        } else {
            configuration.setLayoutDirection(Locale.getDefault())
        }

        return context.createConfigurationContext(configuration)
    }
}