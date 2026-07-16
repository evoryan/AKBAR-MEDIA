import re

with open("app/src/main/java/com/example/ui/navigation/NavGraph.kt", "r") as f:
    content = f.read()

pattern = r"""            composable<AddCustomerRoute> \{
                AddCustomerScreen\(onBack = \{ navController\.popBackStack\(\) \}\)
            \}"""
replacement = """            composable<AddCustomerRoute> {
                AddCustomerScreen(onBack = { navController.popBackStack() })
            }
            
            composable<EditCustomerRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<EditCustomerRoute>()
                EditCustomerScreen(
                    customerId = route.customerId,
                    onBack = { navController.popBackStack() }
                )
            }"""

content = re.sub(pattern, replacement, content)

with open("app/src/main/java/com/example/ui/navigation/NavGraph.kt", "w") as f:
    f.write(content)

