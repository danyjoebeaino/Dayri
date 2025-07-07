package com.danyjoe.lebanesemonateries

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit

class TranslationCacheManager(private val context: Context) {
    private val prefsName = "TranslationCache"
    private val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    fun cacheTranslations(translations: Map<String, Map<String, Map<String, String>>>) {
        val gson = Gson()
        val json = gson.toJson(translations)
        prefs.edit() { putString("monastery_translations", json) }
    }

    fun getTranslationsFromCache(): Map<String, Map<String, Map<String, String>>>? {
        val json = prefs.getString("monastery_translations", null) ?: return null
        val type = object : TypeToken<Map<String, Map<String, Map<String, String>>>>() {}.type
        return Gson().fromJson(json, type)
    }
}