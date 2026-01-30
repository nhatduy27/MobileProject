package com.example.foodapp.utils


import android.annotation.TargetApi
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import java.util.*

object LocaleHelper {
    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"
    private const val SELECTED_COUNTRY = "Locale.Helper.Selected.Country"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    }

    fun setLocale(context: Context, language: String, country: String = "") {
        persist(context, language, country)
        updateResources(context, language, country)
    }

    fun getCurrentLanguage(context: Context): String {
        val prefs = getPreferences(context)
        return prefs.getString(SELECTED_LANGUAGE, "vi") ?: "vi"
    }

    fun isVietnamese(context: Context): Boolean {
        return getCurrentLanguage(context) == "vi"
    }

    fun toggleLanguage(context: Context) {
        val current = getCurrentLanguage(context)
        val newLanguage = if (current == "vi") "en" else "vi"
        val country = if (newLanguage == "vi") "VN" else "US"
        setLocale(context, newLanguage, country)
    }

    private fun persist(context: Context, language: String, country: String) {
        val prefs = getPreferences(context)
        prefs.edit().apply {
            putString(SELECTED_LANGUAGE, language)
            putString(SELECTED_COUNTRY, country)
            apply()
        }
    }

    @Suppress("DEPRECATION")
    private fun updateResources(context: Context, language: String, country: String) {
        val locale = if (country.isNotEmpty()) {
            Locale(language, country)
        } else {
            Locale(language)
        }
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
            configuration.setLayoutDirection(locale)
            context.createConfigurationContext(configuration)
        } else {
            configuration.locale = locale
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                configuration.setLayoutDirection(locale)
            }
        }

        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
}