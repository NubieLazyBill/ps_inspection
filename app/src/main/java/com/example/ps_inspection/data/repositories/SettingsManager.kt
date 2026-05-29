package com.example.ps_inspection.data.repositories

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_HINTS_ENABLED = "hints_enabled"
        private const val DEFAULT_HINTS_ENABLED = true
    }

    fun areHintsEnabled(): Boolean = prefs.getBoolean(KEY_HINTS_ENABLED, DEFAULT_HINTS_ENABLED)

    fun setHintsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HINTS_ENABLED, enabled).apply()
    }
}