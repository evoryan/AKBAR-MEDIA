import re

with open("app/src/main/java/com/example/ui/screens/SettingScreen.kt", "r") as f:
    content = f.read()

# Remove the broken block
pattern = r"@Composable\n\n    if \(showUpdateDialog && updateInfo != null\).*?\}\n\n@Composable"
content = re.sub(pattern, "@Composable", content, flags=re.DOTALL)

# Insert the dialog inside the main SettingScreen Composable
# The main composable ends with Spacer(modifier = Modifier.height(32.dp)) \n } \n } \n }
# Actually, let's find the Logout Button and the end of the Scaffold content

dialog_code = """
    if (showUpdateDialog && updateInfo != null) {
        val latestVersion = updateInfo!!.tag_name.removePrefix("v")
        val isNewer = latestVersion > (currentVersion ?: "0")
        
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            containerColor = cardBg,
            titleContentColor = textMain,
            textContentColor = textSecondary,
            title = { Text(if (isNewer) "Update Tersedia" else "Sudah Versi Terbaru") },
            text = { 
                Column {
                    Text("Versi saat ini: $currentVersion")
                    Text("Versi terbaru: $latestVersion")
                    if (isNewer) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Apakah Anda ingin mengunduh versi terbaru?")
                    }
                }
            },
            confirmButton = {
                if (isNewer) {
                    TextButton(onClick = {
                        val url = updateInfo!!.assets.firstOrNull()?.browser_download_url
                        if (url != null) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, "Link download tidak ditemukan", Toast.LENGTH_SHORT).show()
                        }
                        showUpdateDialog = false
                    }) {
                        Text("Download", color = primaryBg)
                    }
                } else {
                    TextButton(onClick = { showUpdateDialog = false }) {
                        Text("Tutup", color = primaryBg)
                    }
                }
            },
            dismissButton = {
                if (isNewer) {
                    TextButton(onClick = { showUpdateDialog = false }) {
                        Text("Batal", color = textSecondary)
                    }
                }
            }
        )
    }
"""

content = content.replace("            // Logout Button", dialog_code + "\n            // Logout Button")

with open("app/src/main/java/com/example/ui/screens/SettingScreen.kt", "w") as f:
    f.write(content)
