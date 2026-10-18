package com.example.ui.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SettingsManager {
    private const val PREFS_NAME = "app_settings"
    private const val KEY_COMPANY_NAME = "company_name"
    private const val KEY_DASHBOARD_INFO = "dashboard_info"
    private const val KEY_DASHBOARD_INFO_2 = "dashboard_info_2"
    private const val KEY_INVOICE_FOOTER = "invoice_footer"
    private const val KEY_SUPPORT_BY = "support_by"

    private lateinit var prefs: SharedPreferences
    private var appContext: Context? = null

    val appVersionFlow = kotlinx.coroutines.flow.MutableStateFlow("v1.6.1")
    val tenantInfosFlow = kotlinx.coroutines.flow.MutableStateFlow<List<String>>(emptyList())
    val globalAnnouncementsFlow = kotlinx.coroutines.flow.MutableStateFlow<List<com.example.ui.data.remote.GlobalAnnouncement>>(emptyList())

    fun isVersionGreaterOrEqual(v1: String, v2: String): Boolean {
        fun parseParts(v: String): List<Int> {
            val cleaned = v.trim().trimStart('v', 'V', '.').trim()
            val parts = cleaned.split(".").filter { it.isNotBlank() }
            val nums = parts.map { part ->
                part.filter { it.isDigit() }.toIntOrNull() ?: 0
            }
            return if (nums.isEmpty()) listOf(0) else nums
        }
        val p1 = parseParts(v1)
        val p2 = parseParts(v2)
        val maxLen = maxOf(p1.size, p2.size)
        for (i in 0 until maxLen) {
            val n1 = p1.getOrElse(i) { 0 }
            val n2 = p2.getOrElse(i) { 0 }
            if (n1 > n2) return true
            if (n1 < n2) return false
        }
        return true
    }

    fun init(context: Context) {
        appContext = context.applicationContext
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        themeStateFlow.value = appTheme
        fontScaleStateFlow.value = fontScale
        appVersionFlow.value = getEffectiveAppVersion(context)
        globalAnnouncementsFlow.value = globalAnnouncements
        reloadTenantInfosForActiveDatabase()
        restoreInvoiceSettingsIfPresent(context)
        syncSettingsFromRemote(context)
    }

    private const val KEY_APPLIED_APP_VERSION = "applied_app_version"
    var appliedAppVersion: String?
        get() = prefs.getString(KEY_APPLIED_APP_VERSION, null)
        set(value) {
            prefs.edit().putString(KEY_APPLIED_APP_VERSION, value).apply()
            val effective = getEffectiveAppVersion(appContext)
            appVersionFlow.value = effective
            syncSettingsToRemote()
        }

    fun refreshAppVersion(context: Context) {
        val effective = getEffectiveAppVersion(context)
        appVersionFlow.value = effective
    }

    fun getEffectiveAppVersion(context: Context? = null): String {
        val ctx = context ?: appContext
        val packageVersion = try {
            if (ctx != null) {
                val pInfo = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
                pInfo.versionName
            } else null
        } catch (_: Exception) {
            null
        }
        val applied = appliedAppVersion
        if (!applied.isNullOrBlank()) {
            if (packageVersion.isNullOrBlank()) return applied
            val lParts = applied.trim().trimStart('v', 'V', '.').split(".").map { it.filter { c -> c.isDigit() }.toIntOrNull() ?: 0 }
            val pParts = packageVersion.trim().trimStart('v', 'V', '.').split(".").map { it.filter { c -> c.isDigit() }.toIntOrNull() ?: 0 }
            val maxLen = maxOf(lParts.size, pParts.size)
            var packageIsStrictlyNewer = false
            for (i in 0 until maxLen) {
                val l = lParts.getOrElse(i) { 0 }
                val p = pParts.getOrElse(i) { 0 }
                if (p > l) { packageIsStrictlyNewer = true; break }
                if (l > p) { packageIsStrictlyNewer = false; break }
            }
            if (packageIsStrictlyNewer) return packageVersion
            return applied
        }
        return packageVersion ?: "v1.6.1"
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

    private const val KEY_INVOICE_HELP_TEXT = "invoice_help_text"
    var invoiceHelpText: String
        get() = prefs.getString(KEY_INVOICE_HELP_TEXT, "Layanan bantuan & konfirmasi WhatsApp: 0812-3456-7890") ?: "Layanan bantuan & konfirmasi WhatsApp: 0812-3456-7890"
        set(value) {
            prefs.edit().putString(KEY_INVOICE_HELP_TEXT, value).apply()
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
                put("invoice_help_text", prefs.getString(KEY_INVOICE_HELP_TEXT, "Layanan bantuan & konfirmasi WhatsApp: 0812-3456-7890"))
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
            syncSettingsToRemote()
        } catch (e: Exception) {
            android.util.Log.e("SettingsManager", "Error persisting invoice settings: ${e.message}")
        }
    }

    fun syncSettingsToRemote(targetDbName: String? = null) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val currentDb = targetDbName?.takeIf { it.isNotBlank() } ?: getActiveDbName()
                val settingsMap = mutableMapOf<String, String>()
                settingsMap["company_name"] = companyName
                settingsMap["company_slogan"] = companySlogan
                settingsMap["company_address"] = companyAddress
                settingsMap["company_contact"] = companyContact
                settingsMap["invoice_header"] = invoiceHeader
                settingsMap["invoice_footer"] = invoiceFooterText
                settingsMap["invoice_help_text"] = invoiceHelpText
                settingsMap["support_by"] = supportByText
                settingsMap["dashboard_info"] = dashboardInfo1
                settingsMap["dashboard_info_2"] = dashboardInfo2
                settingsMap["app_theme"] = appTheme
                settingsMap["font_scale"] = fontScale
                settingsMap["use_invoice_logo"] = useInvoiceLogo.toString()
                settingsMap["wa_gateway_enabled"] = waGatewayEnabled.toString()
                settingsMap["wa_notify_new_billing"] = waNotifyNewBilling.toString()
                settingsMap["wa_notify_payment_success"] = waNotifyPaymentSuccess.toString()
                settingsMap["wa_notify_isolir"] = waNotifyIsolir.toString()
                settingsMap["wa_notify_otp"] = waNotifyOtp.toString()
                settingsMap["wa_template_new_billing"] = waTemplateNewBilling
                settingsMap["wa_template_payment_success"] = waTemplatePaymentSuccess
                settingsMap["wa_template_isolir"] = waTemplateIsolir
                settingsMap["wa_template_otp"] = waTemplateOtp
                appliedAppVersion?.let { settingsMap["applied_app_version"] = it }
                val key = getTenantInfoPrefsKey(currentDb)
                val tenantJson = prefs.getString(key, "[]") ?: "[]"
                settingsMap["tenant_info_list"] = tenantJson
                settingsMap["tenant_info_list_$currentDb"] = tenantJson
                settingsMap["db_name"] = currentDb

                com.example.ui.data.remote.ApiClient.apiService.saveAppSettings(settingsMap)
                android.util.Log.d("SettingsManager", "Pengaturan aplikasi (Tenant DB: $currentDb) berhasil disimpan ke remote")
            } catch (e: Exception) {
                android.util.Log.w("SettingsManager", "Gagal sync pengaturan ke MySQL: ${e.message}")
            }
        }
    }

    fun syncSettingsFromRemote(context: Context? = null) {
        val ctx = context ?: appContext ?: return
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val remote = com.example.ui.data.remote.ApiClient.apiService.getAppSettings()
                if (remote.isNotEmpty()) {
                    val editor = prefs.edit()
                    remote["company_name"]?.let { if (it.isNotBlank()) editor.putString(KEY_COMPANY_NAME, it) }
                    remote["company_slogan"]?.let { if (it.isNotBlank()) editor.putString(KEY_COMPANY_SLOGAN, it) }
                    remote["company_address"]?.let { if (it.isNotBlank()) editor.putString(KEY_COMPANY_ADDRESS, it) }
                    remote["company_contact"]?.let { if (it.isNotBlank()) editor.putString(KEY_COMPANY_CONTACT, it) }
                    remote["invoice_header"]?.let { if (it.isNotBlank()) editor.putString("invoice_header", it) }
                    remote["invoice_footer"]?.let { if (it.isNotBlank()) editor.putString(KEY_INVOICE_FOOTER, it) }
                    remote["invoice_help_text"]?.let { if (it.isNotBlank()) editor.putString(KEY_INVOICE_HELP_TEXT, it) }
                    remote["support_by"]?.let { if (it.isNotBlank()) editor.putString(KEY_SUPPORT_BY, it) }
                    remote["dashboard_info"]?.let { if (it.isNotBlank()) editor.putString(KEY_DASHBOARD_INFO, it) }
                    remote["dashboard_info_2"]?.let { if (it.isNotBlank()) editor.putString(KEY_DASHBOARD_INFO_2, it) }
                    remote["applied_app_version"]?.let {
                        if (it.isNotBlank()) {
                            val curr = appliedAppVersion
                            if (curr == null || isVersionGreaterOrEqual(it, curr)) {
                                editor.putString(KEY_APPLIED_APP_VERSION, it)
                                appVersionFlow.value = it
                            }
                        }
                    }
                    remote["wa_gateway_enabled"]?.let { editor.putBoolean("wa_gateway_enabled", it.toBoolean()) }
                    remote["wa_notify_new_billing"]?.let { editor.putBoolean("wa_notify_new_billing", it.toBoolean()) }
                    remote["wa_notify_payment_success"]?.let { editor.putBoolean("wa_notify_payment_success", it.toBoolean()) }
                    remote["wa_notify_isolir"]?.let { editor.putBoolean("wa_notify_isolir", it.toBoolean()) }
                    remote["wa_notify_otp"]?.let { editor.putBoolean("wa_notify_otp", it.toBoolean()) }
                    remote["wa_template_new_billing"]?.let { if (it.isNotBlank()) editor.putString("wa_template_new_billing", it) }
                    remote["wa_template_payment_success"]?.let { if (it.isNotBlank()) editor.putString("wa_template_payment_success", it) }
                    remote["wa_template_isolir"]?.let { if (it.isNotBlank()) editor.putString("wa_template_isolir", it) }
                    remote["wa_template_otp"]?.let { if (it.isNotBlank()) editor.putString("wa_template_otp", it) }

                    val currentDb = getActiveDbName()
                    val isolatedKey = "tenant_info_list_$currentDb"
                    val remoteTenantJson = remote[isolatedKey] ?: remote["tenant_info_list"]
                    if (!remoteTenantJson.isNullOrBlank()) {
                        val key = getTenantInfoPrefsKey(currentDb)
                        editor.putString(key, remoteTenantJson)
                        try {
                            val list = kotlinx.serialization.json.Json.decodeFromString<List<String>>(remoteTenantJson)
                            tenantInfosFlow.value = list
                            android.util.Log.d("SettingsManager", "Isolasi Tenant DB: Sinkronisasi remote berhasil untuk [$currentDb] (${list.size} info)")
                        } catch (e: Exception) {
                            // Ignore
                        }
                    }

                    // Sinkronisasi pengumuman global master akbar2026 untuk seluruh user & DB
                    remote["global_master_announcements"]?.let { remoteBroadcastJson ->
                        if (remoteBroadcastJson.isNotBlank()) {
                            editor.putString(KEY_GLOBAL_ANNOUNCEMENTS, remoteBroadcastJson)
                            try {
                                val broadcastList = kotlinx.serialization.json.Json.decodeFromString<List<com.example.ui.data.remote.GlobalAnnouncement>>(remoteBroadcastJson)
                                globalAnnouncementsFlow.value = broadcastList
                                android.util.Log.d("SettingsManager", "Pengumuman Master Global berhasil dimuat: ${broadcastList.size} pesan")
                            } catch (e: Exception) {
                                // Ignore
                            }
                        }
                    }

                    editor.apply()
                    android.util.Log.d("SettingsManager", "Pengaturan aplikasi berhasil disinkronkan dari MySQL")
                }
            } catch (e: Exception) {
                android.util.Log.w("SettingsManager", "Gagal mengambil pengaturan dari MySQL: ${e.message}")
            }
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
                if (json.has("invoice_help_text") && !prefs.contains(KEY_INVOICE_HELP_TEXT)) {
                    editor.putString(KEY_INVOICE_HELP_TEXT, json.getString("invoice_help_text"))
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
        set(value) {
            prefs.edit().putBoolean("wa_gateway_enabled", value).apply()
            syncSettingsToRemote()
        }

    var waNotifyNewBilling: Boolean
        get() = prefs.getBoolean("wa_notify_new_billing", true)
        set(value) {
            prefs.edit().putBoolean("wa_notify_new_billing", value).apply()
            syncSettingsToRemote()
        }

    var waNotifyPaymentSuccess: Boolean
        get() = prefs.getBoolean("wa_notify_payment_success", true)
        set(value) {
            prefs.edit().putBoolean("wa_notify_payment_success", value).apply()
            syncSettingsToRemote()
        }

    var waNotifyIsolir: Boolean
        get() = prefs.getBoolean("wa_notify_isolir", true)
        set(value) {
            prefs.edit().putBoolean("wa_notify_isolir", value).apply()
            syncSettingsToRemote()
        }

    var waNotifyOtp: Boolean
        get() = prefs.getBoolean("wa_notify_otp", true)
        set(value) {
            prefs.edit().putBoolean("wa_notify_otp", value).apply()
            syncSettingsToRemote()
        }

    var waTemplateNewBilling: String
        get() = prefs.getString("wa_template_new_billing", "Halo {nama},\nTagihan internet AKBAR MEDIA Anda untuk bulan {bulan} telah terbit sebesar {nominal}.\n\nHarap segera melakukan pembayaran sebelum jatuh tempo.\n\nTerima kasih.") ?: ""
        set(value) {
            prefs.edit().putString("wa_template_new_billing", value).apply()
            syncSettingsToRemote()
        }

    var waTemplatePaymentSuccess: String
        get() = prefs.getString("wa_template_payment_success", "Halo {nama},\nTerima kasih, pembayaran tagihan internet AKBAR MEDIA untuk bulan {bulan} sejumlah {nominal} telah kami terima dan dinyatakan LUNAS.\n\nSalam,\nAKBAR MEDIA") ?: ""
        set(value) {
            prefs.edit().putString("wa_template_payment_success", value).apply()
            syncSettingsToRemote()
        }

    var waTemplateIsolir: String
        get() = prefs.getString("wa_template_isolir", "Halo {nama},\nLayanan internet Anda sementara dinonaktifkan karena belum melakukan pembayaran tagihan bulan {bulan}.\n\nSilakan lakukan pembayaran untuk mengaktifkan kembali layanan.\n\nTerima kasih.") ?: ""
        set(value) {
            prefs.edit().putString("wa_template_isolir", value).apply()
            syncSettingsToRemote()
        }

    var waTemplateOtp: String
        get() = prefs.getString("wa_template_otp", "Kode OTP Anda adalah: {otp}\n\nJangan membagikan kode ini kepada siapa pun.\n\nAKBAR MEDIA") ?: ""
        set(value) {
            prefs.edit().putString("wa_template_otp", value).apply()
            syncSettingsToRemote()
        }

    fun getActiveDbName(): String {
        return UserSession.currentUser.value?.db_name?.takeIf { it.isNotBlank() }
            ?: appContext?.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                ?.getString("user_db_name", null)?.takeIf { it.isNotBlank() }
            ?: "tenant_default"
    }

    private fun getTenantInfoPrefsKey(dbName: String? = null): String {
        val targetDb = dbName?.takeIf { it.isNotBlank() } ?: getActiveDbName()
        val safeKey = targetDb.replace(Regex("[^a-zA-Z0-9_]"), "_")
        return "tenant_info_list_$safeKey"
    }

    fun getTenantInfosForDb(dbName: String? = null): List<String> {
        val targetDb = dbName?.takeIf { it.isNotBlank() } ?: getActiveDbName()
        val key = getTenantInfoPrefsKey(targetDb)
        val jsonStr = prefs.getString(key, null)
        if (jsonStr != null) {
            return try {
                kotlinx.serialization.json.Json.decodeFromString<List<String>>(jsonStr)
            } catch (e: Exception) {
                emptyList()
            }
        }
        // Fallback for legacy key if this is default database
        if (targetDb == "akbarmedia" || targetDb == "tenant_default") {
            val legacy = prefs.getString("tenant_info_list", null)
            if (!legacy.isNullOrBlank()) {
                return try {
                    val list = kotlinx.serialization.json.Json.decodeFromString<List<String>>(legacy)
                    prefs.edit().putString(key, legacy).apply()
                    list
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }
        return emptyList()
    }

    fun setTenantInfosForDb(value: List<String>, dbName: String? = null) {
        val targetDb = dbName?.takeIf { it.isNotBlank() } ?: getActiveDbName()
        val key = getTenantInfoPrefsKey(targetDb)
        val jsonStr = kotlinx.serialization.json.Json.encodeToString(value)
        prefs.edit().putString(key, jsonStr).apply()
        if (targetDb == getActiveDbName()) {
            tenantInfosFlow.value = value
        }
        syncSettingsToRemote(targetDb)
    }

    var tenantInfos: List<String>
        get() = getTenantInfosForDb(getActiveDbName())
        set(value) {
            setTenantInfosForDb(value, getActiveDbName())
        }

    fun reloadTenantInfosForActiveDatabase(dbName: String? = null) {
        val targetDb = dbName ?: getActiveDbName()
        val list = getTenantInfosForDb(targetDb)
        tenantInfosFlow.value = list
        android.util.Log.d("SettingsManager", "Isolasi Tenant DB: Memuat info terisolasi untuk [$targetDb] (${list.size} item)")
    }

    fun onSessionCleared() {
        tenantInfosFlow.value = emptyList()
    }

    private const val KEY_GLOBAL_ANNOUNCEMENTS = "global_master_announcements"

    var globalAnnouncements: List<com.example.ui.data.remote.GlobalAnnouncement>
        get() {
            if (!::prefs.isInitialized) return emptyList()
            val jsonStr = prefs.getString(KEY_GLOBAL_ANNOUNCEMENTS, "[]") ?: "[]"
            return try {
                kotlinx.serialization.json.Json.decodeFromString(jsonStr)
            } catch (e: Exception) {
                emptyList()
            }
        }
        set(value) {
            val jsonStr = kotlinx.serialization.json.Json.encodeToString(value)
            prefs.edit().putString(KEY_GLOBAL_ANNOUNCEMENTS, jsonStr).apply()
            globalAnnouncementsFlow.value = value
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
