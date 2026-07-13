import re

with open('app/src/main/java/com/example/ui/screens/SettingScreen.kt', 'r') as f:
    content = f.read()

# Update signature
old_sig = """    onNavigateToOdp: () -> Unit,
    onNavigateToGatewayPayment: () -> Unit,
    onLogout: () -> Unit"""

new_sig = """    onNavigateToOdp: () -> Unit,
    onNavigateToGatewayPayment: () -> Unit,
    onNavigateToCompanySettings: () -> Unit,
    onLogout: () -> Unit"""
content = content.replace(old_sig, new_sig)

# Add SettingItem
old_menu = """            // Lain-Lain
            if (currentUser?.role == UserRole.SUPER_ADMIN) {
                Text("LAIN-LAIN", color = primaryBg, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                ) {
                    Column {
                        SettingItem(icon = Icons.Default.Payments, title = "Pengaturan Gateway Payment", subtitle = "Integrasi payment gateway", iconTint = textMain, onClick = onNavigateToGatewayPayment)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }"""

new_menu = """            // TAMPILAN
            if (currentUser?.role == UserRole.SUPER_ADMIN) {
                Text("TAMPILAN", color = primaryBg, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                ) {
                    Column {
                        SettingItem(icon = Icons.Default.Edit, title = "Tampilan Aplikasi", subtitle = "Ubah nama perusahaan & info dashboard", iconTint = textMain, onClick = onNavigateToCompanySettings)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Lain-Lain
            if (currentUser?.role == UserRole.SUPER_ADMIN) {
                Text("LAIN-LAIN", color = primaryBg, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                ) {
                    Column {
                        SettingItem(icon = Icons.Default.Payments, title = "Pengaturan Gateway Payment", subtitle = "Integrasi payment gateway", iconTint = textMain, onClick = onNavigateToGatewayPayment)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }"""
content = content.replace(old_menu, new_menu)

with open('app/src/main/java/com/example/ui/screens/SettingScreen.kt', 'w') as f:
    f.write(content)
