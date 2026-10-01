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
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        themeStateFlow.value = appTheme
        fontScaleStateFlow.value = fontScale
        restoreInvoiceSettingsIfPresent(context)
    }

    private const val KEY_API_BASE_URL = "api_base_url"
    var apiBaseUrl: String
        get() {
            val saved = prefs.getString(KEY_API_BASE_URL, "http://amg.akbarmediagroup.my.id/") ?: "http://amg.akbarmediagroup.my.id/"
            if (saved.contains("api.akbarmediagroup.me") || saved.contains("103.253.245.25")) {
                val migrated = "http://amg.akbarmediagroup.my.id/"
                prefs.edit().putString(KEY_API_BASE_URL, migrated).apply()
                return migrated
            }
            return saved
        }
        set(value) {
            val formatted = if (value.endsWith("/")) value else "$value/"
            prefs.edit().putString(KEY_API_BASE_URL, formatted).apply()
        }

    var companyName: String
        get() = prefs.getString(KEY_COMPANY_NAME, "Akbar Media") ?: "Akbar Media"
        set(value) {
            prefs.edit().putString(KEY_COMPANY_NAME, value).apply()
            persistInvoiceSettings()
        }

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

    private const val KEY_FONT_SCALE = "font_scale"
    var fontScale: String
        get() = prefs.getString(KEY_FONT_SCALE, "Kecil") ?: "Kecil"
        set(value) {
            prefs.edit().putString(KEY_FONT_SCALE, value).apply()
            fontScaleStateFlow.value = value
        }

    val fontScaleStateFlow = kotlinx.coroutines.flow.MutableStateFlow("Kecil")

    var dashboardInfo2: String
        get() = prefs.getString(KEY_DASHBOARD_INFO_2, "Tersedia update router firmware") ?: "Tersedia update router firmware"
        set(value) = prefs.edit().putString(KEY_DASHBOARD_INFO_2, value).apply()

    private const val KEY_COMPANY_SLOGAN = "company_slogan"
    var companySlogan: String
        get() = prefs.getString(KEY_COMPANY_SLOGAN, "PENYEDIA LAYANAN INTERNET BROADBAND & RT/RW NET BERKUALITAS") ?: "PENYEDIA LAYANAN INTERNET BROADBAND & RT/RW NET BERKUALITAS"
        set(value) {
            prefs.edit().putString(KEY_COMPANY_SLOGAN, value).apply()
            persistInvoiceSettings()
        }

    private const val KEY_COMPANY_ADDRESS = "company_address"
    var companyAddress: String
        get() = prefs.getString(KEY_COMPANY_ADDRESS, "Jln. Raya Akbar Media, Indonesia") ?: "Jln. Raya Akbar Media, Indonesia"
        set(value) {
            prefs.edit().putString(KEY_COMPANY_ADDRESS, value).apply()
            persistInvoiceSettings()
        }

    private const val KEY_COMPANY_CONTACT = "company_contact"
    var companyContact: String
        get() = prefs.getString(KEY_COMPANY_CONTACT, "WhatsApp: 0812-3456-7890 • Email: cs@akbarmedia.my.id") ?: "WhatsApp: 0812-3456-7890 • Email: cs@akbarmedia.my.id"
        set(value) {
            prefs.edit().putString(KEY_COMPANY_CONTACT, value).apply()
            persistInvoiceSettings()
        }

    var invoiceHeader: String
        get() = prefs.getString("invoice_header", "$companyName\n$companySlogan\n$companyAddress\n$companyContact") ?: "$companyName\n$companySlogan\n$companyAddress\n$companyContact"
        set(value) {
            prefs.edit().putString("invoice_header", value).apply()
            persistInvoiceSettings()
        }

    var invoiceFooterText: String
        get() = prefs.getString(KEY_INVOICE_FOOTER, "L U N A S") ?: "L U N A S"
        set(value) {
            prefs.edit().putString(KEY_INVOICE_FOOTER, value).apply()
            persistInvoiceSettings()
        }

    var invoiceLogoPath: String?
        get() {
            val saved = prefs.getString("invoice_logo_path", prefs.getString("custom_invoice_template_path", null))
            if (!saved.isNullOrBlank() && java.io.File(saved).exists()) {
                return saved
            }
            appContext?.let { ctx ->
                val localFile = java.io.File(ctx.filesDir, "invoice_kop_logo.png")
                if (localFile.exists()) {
                    return localFile.absolutePath
                }
                val extFile = ctx.getExternalFilesDir(null)?.let { java.io.File(it, "invoice_kop_logo.png") }
                if (extFile != null && extFile.exists()) {
                    try {
                        extFile.copyTo(localFile, overwrite = true)
                        return localFile.absolutePath
                    } catch (_: Exception) {
                        return extFile.absolutePath
                    }
                }
            }
            return saved
        }
        set(value) {
            prefs.edit().putString("invoice_logo_path", value).apply()
            prefs.edit().putString("custom_invoice_template_path", value).apply()
            persistInvoiceSettings()
        }

    var useInvoiceLogo: Boolean
        get() = prefs.getBoolean("use_invoice_logo", true)
        set(value) {
            prefs.edit().putBoolean("use_invoice_logo", value).apply()
            persistInvoiceSettings()
        }

    var useCustomInvoiceTemplate: Boolean
        get() = prefs.getBoolean("use_custom_invoice_template", true)
        set(value) {
            prefs.edit().putBoolean("use_custom_invoice_template", value).apply()
            persistInvoiceSettings()
        }

    var customInvoiceTemplatePath: String?
        get() = invoiceLogoPath
        set(value) {
            invoiceLogoPath = value
        }

    var customInvoiceOverlayData: Boolean
        get() = prefs.getBoolean("custom_invoice_overlay_data", true)
        set(value) {
            prefs.edit().putBoolean("custom_invoice_overlay_data", value).apply()
            persistInvoiceSettings()
        }

    var supportByText: String
        get() = prefs.getString(KEY_SUPPORT_BY, "Toko Ana, PT.Telkom, PT.Citra Selaras Terabit") ?: "Toko Ana, PT.Telkom, PT.Citra Selaras Terabit"
        set(value) {
            prefs.edit().putString(KEY_SUPPORT_BY, value).apply()
            persistInvoiceSettings()
        }

    fun persistInvoiceSettings(context: Context? = null) {
        val ctx = context ?: appContext ?: return
        try {
            val json = org.json.JSONObject().apply {
                put("company_name", prefs.getString(KEY_COMPANY_NAME, "Akbar Media"))
                put("company_slogan", prefs.getString(KEY_COMPANY_SLOGAN, "PENYEDIA LAYANAN INTERNET BROADBAND & RT/RW NET BERKUALITAS"))
                put("company_address", prefs.getString(KEY_COMPANY_ADDRESS, "Jln. Raya Akbar Media, Indonesia"))
                put("company_contact", prefs.getString(KEY_COMPANY_CONTACT, "WhatsApp: 0812-3456-7890 • Email: cs@akbarmedia.my.id"))
                put("invoice_header", prefs.getString("invoice_header", ""))
                put("invoice_footer", prefs.getString(KEY_INVOICE_FOOTER, "L U N A S"))
                put("support_by", prefs.getString(KEY_SUPPORT_BY, "Toko Ana, PT.Telkom, PT.Citra Selaras Terabit"))
                put("use_invoice_logo", prefs.getBoolean("use_invoice_logo", true))
                put("use_custom_invoice_template", prefs.getBoolean("use_custom_invoice_template", true))
                put("custom_invoice_overlay_data", prefs.getBoolean("custom_invoice_overlay_data", true))
                put("invoice_logo_path", prefs.getString("invoice_logo_path", ""))
            }.toString(2)

            // Save to internal storage
            val internalFile = java.io.File(ctx.filesDir, "invoice_settings_persistent.json")
            internalFile.writeText(json)

            // Save copy to external files dir (if available) for multi-level durability across updates
            ctx.getExternalFilesDir(null)?.let { extDir ->
                val extFile = java.io.File(extDir, "invoice_settings_persistent.json")
                extFile.writeText(json)
            }

            // Also backup logo file to external files dir if available
            val localLogo = java.io.File(ctx.filesDir, "invoice_kop_logo.png")
            if (localLogo.exists()) {
                ctx.getExternalFilesDir(null)?.let { extDir ->
                    val extLogo = java.io.File(extDir, "invoice_kop_logo.png")
                    if (!extLogo.exists() || extLogo.length() != localLogo.length()) {
                        localLogo.copyTo(extLogo, overwrite = true)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SettingsManager", "Error persisting invoice settings: ${e.message}")
        }
    }

    private fun restoreInvoiceSettingsIfPresent(context: Context) {
        try {
            val internalFile = java.io.File(context.filesDir, "invoice_settings_persistent.json")
            val extFile = context.getExternalFilesDir(null)?.let { java.io.File(it, "invoice_settings_persistent.json") }
            
            val targetFile = when {
                internalFile.exists() && internalFile.length() > 0 -> internalFile
                extFile != null && extFile.exists() && extFile.length() > 0 -> extFile
                else -> null
            }

            if (targetFile != null) {
                val jsonStr = targetFile.readText()
                val json = org.json.JSONObject(jsonStr)
                val editor = prefs.edit()
                
                if (json.has("company_name") && !prefs.contains(KEY_COMPANY_NAME)) {
                    editor.putString(KEY_COMPANY_NAME, json.getString("company_name"))
                }
                if (json.has("company_slogan") && !prefs.contains(KEY_COMPANY_SLOGAN)) {
                    editor.putString(KEY_COMPANY_SLOGAN, json.getString("company_slogan"))
                }
                if (json.has("company_address") && !prefs.contains(KEY_COMPANY_ADDRESS)) {
                    editor.putString(KEY_COMPANY_ADDRESS, json.getString("company_address"))
                }
                if (json.has("company_contact") && !prefs.contains(KEY_COMPANY_CONTACT)) {
                    editor.putString(KEY_COMPANY_CONTACT, json.getString("company_contact"))
                }
                if (json.has("invoice_header") && !prefs.contains("invoice_header")) {
                    editor.putString("invoice_header", json.getString("invoice_header"))
                }
                if (json.has("invoice_footer") && !prefs.contains(KEY_INVOICE_FOOTER)) {
                    editor.putString(KEY_INVOICE_FOOTER, json.getString("invoice_footer"))
                }
                if (json.has("support_by") && !prefs.contains(KEY_SUPPORT_BY)) {
                    editor.putString(KEY_SUPPORT_BY, json.getString("support_by"))
                }
                if (json.has("use_invoice_logo") && !prefs.contains("use_invoice_logo")) {
                    editor.putBoolean("use_invoice_logo", json.getBoolean("use_invoice_logo"))
                }
                if (json.has("use_custom_invoice_template") && !prefs.contains("use_custom_invoice_template")) {
                    editor.putBoolean("use_custom_invoice_template", json.getBoolean("use_custom_invoice_template"))
                }
                if (json.has("invoice_logo_path") && !prefs.contains("invoice_logo_path")) {
                    val path = json.getString("invoice_logo_path")
                    if (path.isNotBlank()) {
                        editor.putString("invoice_logo_path", path)
                    }
                }
                editor.apply()
            }
        } catch (e: Exception) {
            android.util.Log.e("SettingsManager", "Error restoring invoice settings: ${e.message}")
        }
    }

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

    var tenantInfos: List<String>
        get() {
            val jsonStr = prefs.getString("tenant_info_list", "[]") ?: "[]"
            return try {
                kotlinx.serialization.json.Json.decodeFromString<List<String>>(jsonStr)
            } catch (e: Exception) {
                emptyList()
            }
        }
        set(value) {
            val jsonStr = kotlinx.serialization.json.Json.encodeToString(value)
            prefs.edit().putString("tenant_info_list", jsonStr).apply()
        }

    var lastNotifiedInfo: String
        get() = prefs.getString("last_notified_info", "") ?: ""
        set(value) = prefs.edit().putString("last_notified_info", value).apply()

    var lastSeenGangguanId: Int
        get() = prefs.getInt("last_seen_gangguan_id", 0)
        set(value) = prefs.edit().putInt("last_seen_gangguan_id", value).apply()

    fun getSelectedPort(areaId: String): String {
        return prefs.getString("traffic_selected_port_$areaId", "ether1") ?: "ether1"
    }

    fun setSelectedPort(areaId: String, port: String) {
        prefs.edit().putString("traffic_selected_port_$areaId", port).apply()
    }

    fun getTrafficLastRx(areaId: String): Long {
        return prefs.getLong("traffic_last_rx_$areaId", 0L)
    }

    fun setTrafficLastRx(areaId: String, value: Long) {
        prefs.edit().putLong("traffic_last_rx_$areaId", value).apply()
    }

    fun getTrafficLastTx(areaId: String): Long {
        return prefs.getLong("traffic_last_tx_$areaId", 0L)
    }

    fun setTrafficLastTx(areaId: String, value: Long) {
        prefs.edit().putLong("traffic_last_tx_$areaId", value).apply()
    }

    fun getTrafficAccumRx(areaId: String): Long {
        return prefs.getLong("traffic_accum_rx_$areaId", 0L)
    }

    fun setTrafficAccumRx(areaId: String, value: Long) {
        prefs.edit().putLong("traffic_accum_rx_$areaId", value).apply()
    }

    fun getTrafficAccumTx(areaId: String): Long {
        return prefs.getLong("traffic_accum_tx_$areaId", 0L)
    }

    fun setTrafficAccumTx(areaId: String, value: Long) {
        prefs.edit().putLong("traffic_accum_tx_$areaId", value).apply()
    }
}
