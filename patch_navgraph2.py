import re

with open('app/src/main/java/com/example/ui/navigation/NavGraph.kt', 'r') as f:
    content = f.read()

old_route = """            composable<CustomerDetailRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<CustomerDetailRoute>()
                CustomerDetailScreen(
                    customerId = route.customerId,
                    onBack = { navController.popBackStack() }
                )
            }"""

new_route = """            composable<CustomerDetailRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<CustomerDetailRoute>()
                CustomerDetailScreen(
                    customerId = route.customerId,
                    onBack = { navController.popBackStack() },
                    onNavigateToPayment = { id -> navController.navigate(PaymentRoute(id)) }
                )
            }"""

content = content.replace(old_route, new_route)

with open('app/src/main/java/com/example/ui/navigation/NavGraph.kt', 'w') as f:
    f.write(content)
