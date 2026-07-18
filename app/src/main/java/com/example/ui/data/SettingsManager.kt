package com.example.ui.data

import android.content.Context
import android.content.SharedPreferences

object SettingsManager {
    private const val PREFS_NAME = "app_settings"
    private const val KEY_COMPANY_NAME = "company_name"
    private const val KEY_DASHBOARD_INFO = "dashboard_info"
    private const val KEY_DASHBOARD_INFO_2 = "dashboard_info_2"
    private const val KEY_INVOICE_FOOTER = "invoice_footer"
    private const val KEY_SUPPORT_BY = "support_by"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        themeStateFlow.value = appTheme
    }

    var companyName: String
        get() = prefs.getString(KEY_COMPANY_NAME, "Akbar Media") ?: "Akbar Media"
        set(value) = prefs.edit().putString(KEY_COMPANY_NAME, value).apply()

    var dashboardInfo1: String
        get() = prefs.getString(KEY_DASHBOARD_INFO, "Pemeliharaan server pada 12 Agustus 2026") ?: "Pemeliharaan server pada 12 Agustus 2026"
        set(value) = prefs.edit().putString(KEY_DASHBOARD_INFO, value).apply()
        
    private const val KEY_APP_THEME = "app_theme"
    var appTheme: String
        get() = prefs.getString(KEY_APP_THEME, "Sesuai Sistem") ?: "Sesuai Sistem"
        set(value) {
            prefs.edit().putString(KEY_APP_THEME, value).apply()
            themeStateFlow.value = value
        }

    val themeStateFlow = kotlinx.coroutines.flow.MutableStateFlow("Sesuai Sistem")

    var dashboardInfo2: String
        get() = prefs.getString(KEY_DASHBOARD_INFO_2, "Tersedia update router firmware") ?: "Tersedia update router firmware"
        set(value) = prefs.edit().putString(KEY_DASHBOARD_INFO_2, value).apply()

    var invoiceHeader: String
        get() = prefs.getString("invoice_header", "AKBAR MEDIA\nJln.Raya Akbar Media") ?: "AKBAR MEDIA\nJln.Raya Akbar Media"
        set(value) = prefs.edit().putString("invoice_header", value).apply()

    var invoiceFooterText: String
        get() = prefs.getString(KEY_INVOICE_FOOTER, "L U N A S") ?: "L U N A S"
        set(value) = prefs.edit().putString(KEY_INVOICE_FOOTER, value).apply()

    var supportByText: String
        get() = prefs.getString(KEY_SUPPORT_BY, "Toko Ana, PT.Telkom, PT.Citra Selaras Terabit") ?: "Toko Ana, PT.Telkom, PT.Citra Selaras Terabit"
        set(value) = prefs.edit().putString(KEY_SUPPORT_BY, value).apply()

    // WhatsApp Gateway Settings
    var waGatewayEnabled: Boolean
        get() = prefs.getBoolean("wa_gateway_enabled", true)
        set(value) = prefs.edit().putBoolean("wa_gateway_enabled", value).apply()

    var waNotifyNewBilling: Boolean
        get() = prefs.getBoolean("wa_notify_new_billing", true)
        set(value) = prefs.edit().putBoolean("wa_notify_new_billing", value).apply()

    var waNotifyPaymentSuccess: Boolean
        get() = prefs.getBoolean("wa_notify_payment_success", true)
        set(value) = prefs.edit().putBoolean("wa_notify_payment_success", value).apply()

    var waNotifyIsolir: Boolean
        get() = prefs.getBoolean("wa_notify_isolir", true)
        set(value) = prefs.edit().putBoolean("wa_notify_isolir", value).apply()

    var waNotifyOtp: Boolean
        get() = prefs.getBoolean("wa_notify_otp", true)
        set(value) = prefs.edit().putBoolean("wa_notify_otp", value).apply()

    var waTemplateNewBilling: String
        get() = prefs.getString("wa_template_new_billing", "Halo {nama},\nTagihan internet AKBAR MEDIA Anda untuk bulan {bulan} telah terbit sebesar {nominal}.\n\nHarap segera melakukan pembayaran sebelum jatuh tempo.\n\nTerima kasih.") ?: ""
        set(value) = prefs.edit().putString("wa_template_new_billing", value).apply()

    var waTemplatePaymentSuccess: String
        get() = prefs.getString("wa_template_payment_success", "Halo {nama},\nTerima kasih, pembayaran tagihan internet AKBAR MEDIA untuk bulan {bulan} sejumlah {nominal} telah kami terima dan dinyatakan LUNAS.\n\nSalam,\nAKBAR MEDIA") ?: ""
        set(value) = prefs.edit().putString("wa_template_payment_success", value).apply()

    var waTemplateIsolir: String
        get() = prefs.getString("wa_template_isolir", "Halo {nama},\nLayanan internet Anda sementara dinonaktifkan karena belum melakukan pembayaran tagihan bulan {bulan}.\n\nSilakan lakukan pembayaran untuk mengaktifkan kembali layanan.\n\nTerima kasih.") ?: ""
        set(value) = prefs.edit().putString("wa_template_isolir", value).apply()

    var waTemplateOtp: String
        get() = prefs.getString("wa_template_otp", "Kode OTP Anda adalah: {otp}\n\nJangan membagikan kode ini kepada siapa pun.\n\nAKBAR MEDIA") ?: ""
        set(value) = prefs.edit().putString("wa_template_otp", value).apply()
}
