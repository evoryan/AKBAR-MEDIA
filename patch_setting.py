import re

with open("app/src/main/java/com/example/ui/screens/SettingScreen.kt", "r") as f:
    content = f.read()

target = """            // ODC & ODP
            Text("ODC & ODP","""

rep = """            // TEMA
            Text("TEMA APLIKASI", color = primaryBg, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 8.dp))
            var showThemeDialog by remember { mutableStateOf(false) }
            var currentTheme by remember { mutableStateOf(com.example.ui.data.SettingsManager.appTheme) }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
            ) {
                SettingItem(
                    icon = Icons.Default.Palette, 
                    title = "Tema Aplikasi", 
                    subtitle = currentTheme, 
                    iconTint = textMain, 
                    onClick = { showThemeDialog = true }
                )
            }
            
            if (showThemeDialog) {
                AlertDialog(
                    onDismissRequest = { showThemeDialog = false },
                    title = { Text("Pilih Tema", color = textMain) },
                    containerColor = cardBg,
                    text = {
                        Column {
                            listOf("Sesuai Sistem", "Tema Gelap", "Tema Terang").forEach { themeName ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            com.example.ui.data.SettingsManager.appTheme = themeName
                                            currentTheme = themeName
                                            showThemeDialog = false
                                        }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (themeName == currentTheme),
                                        onClick = null,
                                        colors = RadioButtonDefaults.colors(selectedColor = primaryBg)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(themeName, color = textMain)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showThemeDialog = false }) {
                            Text("Batal", color = primaryBg)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // ODC & ODP
            Text("ODC & ODP","""

if target in content:
    content = content.replace(target, rep)
    with open("app/src/main/java/com/example/ui/screens/SettingScreen.kt", "w") as f:
        f.write(content)
    print("Patched SettingScreen.kt")
else:
    print("Target not found")
