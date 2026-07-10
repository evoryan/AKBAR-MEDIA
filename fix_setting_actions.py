filepath = 'app/src/main/java/com/example/ui/screens/SettingScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Update signature
content = content.replace("fun SettingScreen(onBack: () -> Unit) {", 
"""fun SettingScreen(
    onBack: () -> Unit,
    onNavigateToUpdateEmail: () -> Unit,
    onNavigateToUpdateProfil: () -> Unit,
    onNavigateToGantiPassword: () -> Unit,
    onNavigateToGantiPin: () -> Unit,
    onNavigateToDaftarAdmin: () -> Unit,
    onNavigateToOdc: () -> Unit,
    onNavigateToOdp: () -> Unit,
    onNavigateToGatewayPayment: () -> Unit,
    onLogout: () -> Unit
) {""")

# Update onClick actions
replacements = [
    ('SettingItem(icon = Icons.Default.Email, title = "Update Email", subtitle = "Daftar email admin & verifikasi", iconTint = textMain, onClick = { /* TODO */ })',
     'SettingItem(icon = Icons.Default.Email, title = "Update Email", subtitle = "Daftar email admin & verifikasi", iconTint = textMain, onClick = onNavigateToUpdateEmail)'),
     
    ('SettingItem(icon = Icons.Default.Person, title = "Update Profil", subtitle = "Foto profil, Nama, Username", iconTint = textMain, onClick = { /* TODO */ })',
     'SettingItem(icon = Icons.Default.Person, title = "Update Profil", subtitle = "Foto profil, Nama, Username", iconTint = textMain, onClick = onNavigateToUpdateProfil)'),
     
    ('SettingItem(icon = Icons.Default.Lock, title = "Ganti Password", subtitle = "Ubah password login", iconTint = textMain, onClick = { /* TODO */ })',
     'SettingItem(icon = Icons.Default.Lock, title = "Ganti Password", subtitle = "Ubah password login", iconTint = textMain, onClick = onNavigateToGantiPassword)'),
     
    ('SettingItem(icon = Icons.Default.Dialpad, title = "Ganti PIN", subtitle = "PIN verifikasi hapus transaksi", iconTint = textMain, onClick = { /* TODO */ })',
     'SettingItem(icon = Icons.Default.Dialpad, title = "Ganti PIN", subtitle = "PIN verifikasi hapus transaksi", iconTint = textMain, onClick = onNavigateToGantiPin)'),
     
    ('SettingItem(icon = Icons.Default.Group, title = "Daftar Admin", subtitle = "Kelola akun admin/teknisi/collector", iconTint = textMain, onClick = { /* TODO */ })',
     'SettingItem(icon = Icons.Default.Group, title = "Daftar Admin", subtitle = "Kelola akun admin/teknisi/collector", iconTint = textMain, onClick = onNavigateToDaftarAdmin)'),
     
    ('SettingItem(icon = Icons.Default.DeviceHub, title = "ODC", subtitle = "Kelola ODC", iconTint = textMain, onClick = { /* TODO */ })',
     'SettingItem(icon = Icons.Default.DeviceHub, title = "ODC", subtitle = "Kelola ODC", iconTint = textMain, onClick = onNavigateToOdc)'),
     
    ('SettingItem(icon = Icons.Default.Hub, title = "ODP", subtitle = "Kelola ODP", iconTint = textMain, onClick = { /* TODO */ })',
     'SettingItem(icon = Icons.Default.Hub, title = "ODP", subtitle = "Kelola ODP", iconTint = textMain, onClick = onNavigateToOdp)'),
     
    ('SettingItem(icon = Icons.Default.Payments, title = "Pengaturan Gateway Payment", subtitle = "Integrasi payment gateway", iconTint = textMain, onClick = { /* TODO */ })',
     'SettingItem(icon = Icons.Default.Payments, title = "Pengaturan Gateway Payment", subtitle = "Integrasi payment gateway", iconTint = textMain, onClick = onNavigateToGatewayPayment)'),
     
    ('onClick = { /* TODO Logout */ }', 'onClick = onLogout')
]

for old, new in replacements:
    content = content.replace(old, new)

with open(filepath, 'w') as f:
    f.write(content)
