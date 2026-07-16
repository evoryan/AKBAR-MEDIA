import re

with open("app/src/main/java/com/example/ui/screens/SettingScreen.kt", "r") as f:
    content = f.read()

imports_to_add = """import android.content.Intent
import android.net.Uri
import android.content.pm.PackageInfo
import androidx.compose.material.icons.filled.SystemUpdate
import kotlinx.coroutines.launch
import com.example.data.GithubApiService
import com.example.data.GithubRelease
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
"""

# Insert imports
if "import android.content.Intent" not in content:
    content = content.replace("import androidx.compose.foundation.layout.*", imports_to_add + "import androidx.compose.foundation.layout.*")

# State for update dialog
state_declarations = """    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<GithubRelease?>(null) }
    var isCheckingUpdate by remember { mutableStateOf(false) }
    
    val currentVersion = remember {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName
        } catch (e: Exception) {
            "1.0"
        }
    }
"""

if "var showUpdateDialog" not in content:
    content = content.replace("    val context = LocalContext.current", state_declarations, 1)
    if "var showUpdateDialog" not in content: # If LocalContext wasn't found
        content = content.replace("    var showThemeDialog by remember { mutableStateOf(false) }", state_declarations + "    var showThemeDialog by remember { mutableStateOf(false) }")

system_section = """            // SISTEM
            Text("SISTEM", color = primaryBg, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
            ) {
                Column {
                    SettingItem(
                        icon = Icons.Default.SystemUpdate, 
                        title = "Cek Update", 
                        subtitle = if (isCheckingUpdate) "Memeriksa..." else "Versi $currentVersion", 
                        iconTint = textMain, 
                        onClick = {
                            if (!isCheckingUpdate) {
                                isCheckingUpdate = true
                                coroutineScope.launch {
                                    try {
                                        val api = GithubApiService.create()
                                        // TODO: Ganti owner dan repo dengan yang sesuai
                                        val release = api.getLatestRelease("satriaevo", "VPS_Backend") // placeholder, will use appropriate later
                                        updateInfo = release
                                        showUpdateDialog = true
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Gagal memeriksa update: ${e.message}", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isCheckingUpdate = false
                                    }
                                }
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            // Logout Button"""

content = content.replace("            // Logout Button", system_section)

dialog_section = """
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

if "showUpdateDialog && updateInfo != null" not in content:
    content = content.replace("fun SettingItem", dialog_section + "\n@Composable\nfun SettingItem")

with open("app/src/main/java/com/example/ui/screens/SettingScreen.kt", "w") as f:
    f.write(content)

