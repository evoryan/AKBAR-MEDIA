import re

filepath = 'app/src/main/java/com/example/ui/navigation/NavGraph.kt'
with open(filepath, 'r') as f:
    content = f.read()

old_ganti_pw = "composable<GantiPasswordRoute> { GantiPasswordScreen(onBack = { navController.popBackStack() }) }"
new_ganti_pw = """composable<GantiPasswordRoute> { 
                GantiPasswordScreen(
                    onBack = { navController.popBackStack() },
                    onPasswordChanged = {
                        navController.navigate(LoginRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) 
            }"""

if old_ganti_pw in content:
    content = content.replace(old_ganti_pw, new_ganti_pw)
    with open(filepath, 'w') as f:
        f.write(content)
