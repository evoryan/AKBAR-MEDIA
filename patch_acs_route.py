import re

with open('app/src/main/java/com/example/ui/navigation/Routes.kt', 'r') as f:
    content = f.read()
content = content.replace('object AcsRoute', 'data class AcsRoute(val searchQuery: String = "")')
with open('app/src/main/java/com/example/ui/navigation/Routes.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/navigation/NavGraph.kt', 'r') as f:
    content = f.read()

old_nav_acs = 'onNavigateToAcs = { navController.navigate(AcsRoute) },'
new_nav_acs = 'onNavigateToAcs = { navController.navigate(AcsRoute()) },'
content = content.replace(old_nav_acs, new_nav_acs)

old_comp_acs = """            composable<AcsRoute> {
                AcsScreen(onBack = { navController.popBackStack() })
            }"""
new_comp_acs = """            composable<AcsRoute> { backStackEntry ->
                val query = backStackEntry.toRoute<AcsRoute>().searchQuery
                AcsScreen(onBack = { navController.popBackStack() }, initialSearchQuery = query)
            }"""
content = content.replace(old_comp_acs, new_comp_acs)

with open('app/src/main/java/com/example/ui/navigation/NavGraph.kt', 'w') as f:
    f.write(content)
