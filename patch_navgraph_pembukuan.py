with open('app/src/main/java/com/example/ui/navigation/NavGraph.kt', 'r') as f:
    content = f.read()

content = content.replace("onNavigateToSemuaPembukuan = { navController.navigate(SemuaPembukuanRoute()) }", "onNavigateToSemuaPembukuan = { type -> navController.navigate(SemuaPembukuanRoute(type)) }")
content = content.replace("PembukuanScreen(\n                    onBack = { navController.popBackStack() },", "PembukuanScreen(\n                    onNavigateToBilling = { tab -> navController.navigate(BillingRoute(tab)) },\n                    onBack = { navController.popBackStack() },")

with open('app/src/main/java/com/example/ui/navigation/NavGraph.kt', 'w') as f:
    f.write(content)
