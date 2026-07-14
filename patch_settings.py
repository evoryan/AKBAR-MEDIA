import re

with open("app/src/main/java/com/example/ui/data/SettingsManager.kt", "r") as f:
    content = f.read()

addition = """
    private const val KEY_INVOICE_FOOTER = "invoice_footer"
    private const val KEY_SUPPORT_BY = "support_by"

    var invoiceFooterText: String
        get() = prefs.getString(KEY_INVOICE_FOOTER, "L U N A S") ?: "L U N A S"
        set(value) = prefs.edit().putString(KEY_INVOICE_FOOTER, value).apply()

    var supportByText: String
        get() = prefs.getString(KEY_SUPPORT_BY, "Toko Ana, PT.Telkom, PT.Citra Selaras Terabit") ?: "Toko Ana, PT.Telkom, PT.Citra Selaras Terabit"
        set(value) = prefs.edit().putString(KEY_SUPPORT_BY, value).apply()
}
"""

content = content.replace("}", addition)

with open("app/src/main/java/com/example/ui/data/SettingsManager.kt", "w") as f:
    f.write(content)

