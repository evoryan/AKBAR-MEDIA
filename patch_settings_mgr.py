import re

with open("app/src/main/java/com/example/ui/data/SettingsManager.kt", "r") as f:
    content = f.read()

target = """    var dashboardInfo2: String"""

rep = """    private const val KEY_APP_THEME = "app_theme"

    var appTheme: String
        get() = prefs.getString(KEY_APP_THEME, "Sesuai Sistem") ?: "Sesuai Sistem"
        set(value) {
            prefs.edit().putString(KEY_APP_THEME, value).apply()
            themeStateFlow.value = value
        }

    val themeStateFlow = kotlinx.coroutines.flow.MutableStateFlow("Sesuai Sistem")

    var dashboardInfo2: String"""

if target in content:
    content = content.replace(target, rep)
    
    # Also initialize themeStateFlow in init
    target_init = """prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }"""
    rep_init = """prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        themeStateFlow.value = appTheme
    }"""
    content = content.replace(target_init, rep_init)
    
    with open("app/src/main/java/com/example/ui/data/SettingsManager.kt", "w") as f:
        f.write(content)
    print("Patched SettingsManager.kt")
else:
    print("Target not found")
