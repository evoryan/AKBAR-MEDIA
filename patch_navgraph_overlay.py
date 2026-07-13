import re

with open('app/src/main/java/com/example/ui/navigation/NavGraph.kt', 'r') as f:
    content = f.read()

imports = """import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.example.ui.data.remote.ApiClient
"""

if "import kotlinx.coroutines.delay" not in content:
    content = content.replace("import androidx.navigation.toRoute\n", "import androidx.navigation.toRoute\n" + imports)

# Find the start of AkbarMediaNavGraph
old_graph_start = """fun AkbarMediaNavGraph() {
    val navController = rememberNavController()"""

new_graph_start = """fun AkbarMediaNavGraph() {
    val navController = rememberNavController()
    var isServerOnline by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while(true) {
            try {
                ApiClient.apiService.ping()
                isServerOnline = true
            } catch(e: Exception) {
                isServerOnline = false
            }
            delay(5000)
        }
    }"""

content = content.replace(old_graph_start, new_graph_start)

# Add overlay
old_box = """    Box(modifier = Modifier.fillMaxSize()) {
        NavHost("""
new_box = """    Box(modifier = Modifier.fillMaxSize()) {
        NavHost("""
content = content.replace(old_box, new_box)

old_bottom = """        if (showBottomNav) {
            Box(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                FloatingNavBar(
                    currentRoute = currentDestination,
                    onNavigateToDashboard = { 
                        navController.navigate(DashboardRoute) {
                            popUpTo(DashboardRoute) { inclusive = true }
                        }
                    },
                    onNavigateToCustomers = { navController.navigate(CustomersRoute) },
                    onNavigateToBilling = { navController.navigate(BillingRoute()) },
                    onNavigateToSettings = { navController.navigate(SettingRoute) }
                )
            }
        }
    }"""
new_bottom = """        if (showBottomNav) {
            Box(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                FloatingNavBar(
                    currentRoute = currentDestination,
                    onNavigateToDashboard = { 
                        navController.navigate(DashboardRoute) {
                            popUpTo(DashboardRoute) { inclusive = true }
                        }
                    },
                    onNavigateToCustomers = { navController.navigate(CustomersRoute) },
                    onNavigateToBilling = { navController.navigate(BillingRoute()) },
                    onNavigateToSettings = { navController.navigate(SettingRoute) }
                )
            }
        }
        
        // Online/Offline Indicator (Visible on all screens)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
                .statusBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (isServerOnline) Color.Green else Color.Red)
            )
        }
    }"""
content = content.replace(old_bottom, new_bottom)

with open('app/src/main/java/com/example/ui/navigation/NavGraph.kt', 'w') as f:
    f.write(content)
