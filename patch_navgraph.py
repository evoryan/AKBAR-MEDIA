import re

with open('app/src/main/java/com/example/ui/navigation/NavGraph.kt', 'r') as f:
    content = f.read()

replacement = """            composable<SplashRoute> {
                SplashScreen(
                    onNavigateToLogin = {
                        navController.navigate(LoginRoute) {
                            popUpTo(SplashRoute) { inclusive = true }
                        }
                    },
                    onNavigateToDashboard = {
                        navController.navigate(DashboardRoute) {
                            popUpTo(SplashRoute) { inclusive = true }
                        }
                    }
                )
            }"""

content = re.sub(
    r'composable<SplashRoute> \{\s*SplashScreen\(\s*onNavigateToLogin = \{\s*navController.navigate\(LoginRoute\) \{\s*popUpTo\(SplashRoute\) \{ inclusive = true \}\s*\}\s*\}\s*\)\s*\}',
    replacement,
    content
)

with open('app/src/main/java/com/example/ui/navigation/NavGraph.kt', 'w') as f:
    f.write(content)
