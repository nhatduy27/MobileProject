package com.example.foodapp.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

/**
 * Language Manager - Quản lý đa ngôn ngữ cho ứng dụng
 * Sync với LocaleHelper để đảm bảo tính nhất quán
 */
object LanguageManager {
    
    // Sử dụng cùng SharedPreferences và key với LocaleHelper
    private const val PREFS_NAME = "app_preferences"
    private const val LANGUAGE_KEY = "Locale.Helper.Selected.Language"
    
    /**
     * Các ngôn ngữ được hỗ trợ
     */
    enum class Language(val code: String, val displayName: String) {
        VIETNAMESE("vi", "Tiếng Việt"),
        ENGLISH("en", "English")
    }
    
    /**
     * Lấy ngôn ngữ hiện tại đã lưu
     */
    fun getCurrentLanguage(context: Context): Language {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedCode = prefs.getString(LANGUAGE_KEY, Language.VIETNAMESE.code)
        return Language.values().find { it.code == savedCode } ?: Language.VIETNAMESE
    }
    
    /**
     * Lưu ngôn ngữ đã chọn
     */
    fun saveLanguage(context: Context, language: Language) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(LANGUAGE_KEY, language.code)
            putString("Locale.Helper.Selected.Country", if (language.code == "vi") "VN" else "US")
            apply()
        }
        
        // Apply locale immediately
        applyLocale(context, language)
    }
    
    /**
     * Áp dụng locale ngay lập tức
     */
    @Suppress("DEPRECATION")
    private fun applyLocale(context: Context, language: Language) {
        val locale = Locale(language.code, if (language.code == "vi") "VN" else "US")
        Locale.setDefault(locale)
        
        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
            configuration.setLayoutDirection(locale)
        } else {
            configuration.locale = locale
        }
        
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
    
    /**
     * Áp dụng ngôn ngữ cho context
     */
    fun applyLanguage(context: Context, language: Language): Context {
        val locale = Locale(language.code)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        return context.createConfigurationContext(config)
    }
    
    /**
     * Đặt ngôn ngữ và restart Activity
     */
    fun setLanguage(activity: Activity, language: Language) {
        saveLanguage(activity, language)
        
        // Recreate activity để áp dụng ngôn ngữ mới
        activity.recreate()
    }
    
    /**
     * Wrap context với ngôn ngữ đã lưu
     */
    fun wrapContext(context: Context): Context {
        val language = getCurrentLanguage(context)
        return applyLanguage(context, language)
    }
}

