import re

with open('app/src/main/java/com/example/ui/navigation/NavGraph.kt', 'r') as f:
    content = f.read()

replacement = """                    onLogout = {
                        val context = navController.context
                        com.example.ui.data.UserSession.clearSession(context)
                        navController.navigate(LoginRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    }"""

content = re.sub(
    r'onLogout = \{\s*navController.navigate\(LoginRoute\) \{\s*popUpTo\(0\) \{ inclusive = true \}\s*\}\s*\}',
    replacement,
    content
)

with open('app/src/main/java/com/example/ui/navigation/NavGraph.kt', 'w') as f:
    f.write(content)
