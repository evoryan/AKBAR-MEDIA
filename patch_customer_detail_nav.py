import re

with open('app/src/main/java/com/example/ui/navigation/NavGraph.kt', 'r') as f:
    content = f.read()

old_cd_comp = """                CustomerDetailScreen(
                    customerId = route.customerId,
                    onBack = { navController.popBackStack() },
                    onNavigateToPayment = { id -> navController.navigate(PaymentRoute(id)) }
                )"""

new_cd_comp = """                CustomerDetailScreen(
                    customerId = route.customerId,
                    onBack = { navController.popBackStack() },
                    onNavigateToPayment = { id -> navController.navigate(PaymentRoute(id)) },
                    onNavigateToAcs = { query -> navController.navigate(AcsRoute(query)) }
                )"""

content = content.replace(old_cd_comp, new_cd_comp)

with open('app/src/main/java/com/example/ui/navigation/NavGraph.kt', 'w') as f:
    f.write(content)
