sed -i 's/Box(modifier = Modifier.fillMaxSize()) {/androidx.compose.material3.Scaffold(containerColor = androidx.compose.ui.graphics.Color.Transparent, bottomBar = { if (showBottomNav) FloatingNavBar(currentDestination, { navController.navigate(DashboardRoute) { popUpTo(DashboardRoute) { inclusive = true } } }, { navController.navigate(CustomersRoute) }, { navController.navigate(BillingRoute) }, {}) }) { innerPadding ->/' app/src/main/java/com/example/ui/navigation/NavGraph.kt

sed -i 's/modifier = Modifier.fillMaxSize()/modifier = Modifier.fillMaxSize().padding(bottom = if (showBottomNav) 100.dp else 0.dp)/' app/src/main/java/com/example/ui/navigation/NavGraph.kt

