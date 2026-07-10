with open('app/src/main/java/com/example/ui/screens/SplashScreen.kt', 'r') as f:
    content = f.read()

new_content = content.replace(
    "fun SplashScreen(onNavigateToLogin: () -> Unit) {",
    "fun SplashScreen(onNavigateToLogin: () -> Unit, onNavigateToDashboard: () -> Unit) {"
).replace(
    "delay(2000L) // Show splash for 2 seconds\n        onNavigateToLogin()",
    """delay(2000L) // Show splash for 2 seconds
        val isLoggedIn = com.example.ui.data.UserSession.loadSession(androidx.compose.ui.platform.LocalContext.current)
        if (isLoggedIn) {
            onNavigateToDashboard()
        } else {
            onNavigateToLogin()
        }"""
)

with open('app/src/main/java/com/example/ui/screens/SplashScreen.kt', 'w') as f:
    f.write(new_content)
