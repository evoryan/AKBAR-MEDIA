package com.example.ui.data

import android.content.Context
import android.content.SharedPreferences

object SettingsManager {
    private const val PREFS_NAME = "app_settings"
    private const val KEY_COMPANY_NAME = "company_name"
    private const val KEY_DASHBOARD_INFO = "dashboard_info"
    private const val KEY_DASHBOARD_INFO_2 = "dashboard_info_2"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var companyName: String
        get() = prefs.getString(KEY_COMPANY_NAME, "Akbar Media") ?: "Akbar Media"
        set(value) = prefs.edit().putString(KEY_COMPANY_NAME, value).apply()

    var dashboardInfo1: String
        get() = prefs.getString(KEY_DASHBOARD_INFO, "Pemeliharaan server pada 12 Agustus 2026") ?: "Pemeliharaan server pada 12 Agustus 2026"
        set(value) = prefs.edit().putString(KEY_DASHBOARD_INFO, value).apply()
        
    var dashboardInfo2: String
        get() = prefs.getString(KEY_DASHBOARD_INFO_2, "Tersedia update router firmware") ?: "Tersedia update router firmware"
        set(value) = prefs.edit().putString(KEY_DASHBOARD_INFO_2, value).apply()
}
