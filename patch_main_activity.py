import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = re.sub(
    r"setContent\s*\{\s*AppTheme\s*\{\s*AkbarMediaNavGraph\(\)\s*\}\s*\}",
    """setContent {
         val themeSetting = com.example.ui.data.SettingsManager.themeStateFlow.collectAsState().value
         val darkTheme = when (themeSetting) {
             "Tema Gelap" -> true
             "Tema Terang" -> false
             else -> androidx.compose.foundation.isSystemInDarkTheme()
         }
         AppTheme(darkTheme = darkTheme) {
             AkbarMediaNavGraph()
         }
     }""",
    content
)

content = content.replace("import androidx.activity.compose.setContent", "import androidx.activity.compose.setContent\nimport androidx.compose.runtime.collectAsState")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
print("Patched MainActivity.kt using regex")
