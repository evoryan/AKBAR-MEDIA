with open('app/src/main/java/com/example/ui/screens/SplashScreen.kt', 'r') as f:
    content = f.read()

new_content = content.replace(
    """delay(2000L) // Show splash for 2 seconds
        val isLoggedIn = com.example.ui.data.UserSession.loadSession(androidx.compose.ui.platform.LocalContext.current)
        if (isLoggedIn) {
            onNavigateToDashboard()
        } else {
            onNavigateToLogin()
        }""",
    """delay(2000L) // Show splash for 2 seconds
        val isLoggedIn = com.example.ui.data.UserSession.loadSession(context)
        if (isLoggedIn) {
            onNavigateToDashboard()
        } else {
            onNavigateToLogin()
        }"""
)

new_content = new_content.replace(
    "fun SplashScreen(onNavigateToLogin: () -> Unit, onNavigateToDashboard: () -> Unit) {",
    "fun SplashScreen(onNavigateToLogin: () -> Unit, onNavigateToDashboard: () -> Unit) {\n    val context = androidx.compose.ui.platform.LocalContext.current"
)

with open('app/src/main/java/com/example/ui/screens/SplashScreen.kt', 'w') as f:
    f.write(new_content)
