import re

with open('app/src/main/java/com/example/ui/navigation/NavGraph.kt', 'r') as f:
    content = f.read()

if "object RasioRoute" not in content:
    content = content.replace("@Serializable\nobject OdpRoute", "@Serializable\nobject RasioRoute\n@Serializable\nobject OdpRoute")

if "onNavigateToRasio = { navController.navigate(RasioRoute) }" not in content:
    content = content.replace("onNavigateToOdp = { navController.navigate(OdpRoute) },", "onNavigateToOdp = { navController.navigate(OdpRoute) },\n                    onNavigateToRasio = { navController.navigate(RasioRoute) },")

rasio_composable = """
            composable<RasioRoute> {
                com.example.ui.screens.RasioScreen(
                    onBack = { navController.popBackStack() }
                )
            }
"""
if "RasioScreen" not in content:
    content = content.replace("composable<OdpRoute> {", rasio_composable + "\n            composable<OdpRoute> {")

with open('app/src/main/java/com/example/ui/navigation/NavGraph.kt', 'w') as f:
    f.write(content)
