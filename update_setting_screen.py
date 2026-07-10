import re

filepath = 'app/src/main/java/com/example/ui/screens/SettingScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

if "import com.example.ui.data.UserSession" not in content:
    content = content.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp\nimport com.example.ui.data.UserSession\nimport com.example.ui.data.UserRole")

if "val currentUser by UserSession.currentUser.collectAsState()" not in content:
    content = content.replace("val cardBorder = Color(0xFF333333)", "val cardBorder = Color(0xFF333333)\n    val currentUser by UserSession.currentUser.collectAsState()")

# Update SettingScreen to conditionally show items
# Ganti PIN
content = content.replace(
    'SettingItem(icon = Icons.Default.Dialpad, title = "Ganti PIN", subtitle = "PIN verifikasi hapus transaksi", iconTint = textMain, onClick = onNavigateToGantiPin)',
    'if (currentUser?.role == UserRole.SUPER_ADMIN) { SettingItem(icon = Icons.Default.Dialpad, title = "Ganti PIN", subtitle = "PIN verifikasi hapus transaksi", iconTint = textMain, onClick = onNavigateToGantiPin) }'
)
content = content.replace('HorizontalDivider(color = cardBorder)\n                    if (currentUser?.role == UserRole.SUPER_ADMIN) { SettingItem(icon = Icons.Default.Dialpad', 'if (currentUser?.role == UserRole.SUPER_ADMIN) { HorizontalDivider(color = cardBorder)\n                    SettingItem(icon = Icons.Default.Dialpad')
content = content.replace('onClick = onNavigateToGantiPin) }', 'onClick = onNavigateToGantiPin) }\n                    ')

# Daftar Admin
content = content.replace(
    'SettingItem(icon = Icons.Default.Group, title = "Daftar Admin", subtitle = "Kelola akun admin/teknisi/collector", iconTint = textMain, onClick = onNavigateToDaftarAdmin)',
    'if (currentUser?.role == UserRole.SUPER_ADMIN) { SettingItem(icon = Icons.Default.Group, title = "Daftar Admin", subtitle = "Kelola akun admin/teknisi/collector", iconTint = textMain, onClick = onNavigateToDaftarAdmin) }'
)
content = content.replace('HorizontalDivider(color = cardBorder)\n                    if (currentUser?.role == UserRole.SUPER_ADMIN) { SettingItem(icon = Icons.Default.Group', 'if (currentUser?.role == UserRole.SUPER_ADMIN) { HorizontalDivider(color = cardBorder)\n                    SettingItem(icon = Icons.Default.Group')

# Gateway Payment
content = content.replace(
    '''// Lain-Lain
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

            Spacer(modifier = Modifier.height(32.dp))''',
    '''// Lain-Lain
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
            }'''
)

with open(filepath, 'w') as f:
    f.write(content)
