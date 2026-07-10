import re

filepath = 'app/src/main/java/com/example/ui/screens/DashboardScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Add imports if not present
if "com.example.ui.data.UserSession" not in content:
    content = content.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp\nimport com.example.ui.data.UserSession\nimport com.example.ui.data.UserRole")

# In DashboardScreen function, collect currentUser
if "val currentUser by UserSession.currentUser.collectAsState()" not in content:
    content = content.replace("val uiState by viewModel.uiState.collectAsState()", "val uiState by viewModel.uiState.collectAsState()\n    val currentUser by UserSession.currentUser.collectAsState()")

# Update menu visibility
# Teknisi : hanya tampilkan menu pelanggan, mikrotik, acs, pembukuan, stock barang dan setting
# Collector : hanya tampilkan menu pelanggan, tagihan, ACS, pembukuan, Setting

menus_replacement = """
            // Menus
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MenuItem(icon = Icons.Default.Person, title = "Pelanggan", tint = primaryBg, onClick = onNavigateToCustomers)
                if (currentUser?.role != UserRole.TEKNISI) {
                    MenuItem(icon = Icons.Default.Receipt, title = "Tagihan", tint = primaryBg, onClick = onNavigateToBilling)
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                if (currentUser?.role != UserRole.COLLECTOR) {
                    MenuItem(icon = Icons.Default.Router, title = "Mikrotik", tint = primaryBg, onClick = onNavigateToMikrotik)
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                if (currentUser?.role == UserRole.SUPER_ADMIN || currentUser?.role == UserRole.ADMIN) {
                    MenuItem(icon = Icons.Default.Inventory, title = "Paket", tint = primaryBg, onClick = onNavigateToPackages)
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentUser?.role == UserRole.SUPER_ADMIN || currentUser?.role == UserRole.ADMIN) {
                    MenuItem(icon = Icons.Default.Map, title = "Area", tint = primaryBg, onClick = onNavigateToArea)
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                MenuItem(icon = Icons.Default.Hub, title = "ACS", tint = primaryBg, onClick = onNavigateToAcs)
                if (currentUser?.role == UserRole.SUPER_ADMIN || currentUser?.role == UserRole.ADMIN) {
                    MenuItem(icon = Icons.Default.Message, title = "Bot WA", tint = primaryBg, onClick = onNavigateToBotWa)
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                MenuItem(icon = Icons.Default.MenuBook, title = "Pembukuan", tint = primaryBg, onClick = onNavigateToPembukuan)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                if (currentUser?.role != UserRole.COLLECTOR) {
                    MenuItem(icon = Icons.Default.Inventory2, title = "Stock Brg", tint = primaryBg, onClick = onNavigateToStockBarang)
                    Spacer(modifier = Modifier.width(32.dp))
                }
                MenuItem(icon = Icons.Default.Settings, title = "Setting", tint = primaryBg, onClick = onNavigateToSetting)
            }
"""

# Try to replace the menus block
# Find the first Row containing menus
start_idx = content.find("Row(\n                modifier = Modifier.fillMaxWidth(),\n                horizontalArrangement = Arrangement.SpaceBetween\n            ) {\n                MenuItem(icon = Icons.Default.Person")
end_idx = content.find("            // Recent Transactions")
if start_idx != -1 and end_idx != -1:
    content = content[:start_idx] + menus_replacement.strip() + "\n\n" + content[end_idx:]

with open(filepath, 'w') as f:
    f.write(content)
