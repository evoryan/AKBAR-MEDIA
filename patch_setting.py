import re
with open("app/src/main/java/com/example/ui/screens/SettingScreen.kt", "r") as f:
    content = f.read()

# Make sure imports exist
imports = """
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
"""
if "import java.io.File" not in content:
    content = content.replace("import android.widget.Toast", "import android.widget.Toast\n" + imports)

download_utils = """
suspend fun downloadApk(
    context: android.content.Context,
    url: String,
    onProgress: (Float) -> Unit
): File? {
    return withContext(Dispatchers.IO) {
        try {
            val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()
            
            if (connection.responseCode != java.net.HttpURLConnection.HTTP_OK) {
                return@withContext null
            }
            
            val fileLength = connection.contentLength
            val downloadDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
            if (downloadDir != null && !downloadDir.exists()) {
                downloadDir.mkdirs()
            }
            val apkFile = File(downloadDir, "update.apk")
            
            val input = connection.inputStream
            val output = java.io.FileOutputStream(apkFile)
            
            val data = ByteArray(4096)
            var total: Long = 0
            var count: Int
            
            while (input.read(data).also { count = it } != -1) {
                total += count
                if (fileLength > 0) {
                    onProgress((total.toFloat() / fileLength.toFloat()))
                }
                output.write(data, 0, count)
            }
            output.flush()
            output.close()
            input.close()
            
            apkFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

fun installApk(context: android.content.Context, apkFile: File) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
    val apkUri = androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        apkFile
    )
    intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal menginstall update: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
"""

if "suspend fun downloadApk" not in content:
    content = content + "\n" + download_utils

# Now we need to modify the update dialog part

dialog_target = """    if (showUpdateDialog && updateInfo != null) {
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
                        Text("Batal", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666))
                    }
                }
            }
        )
    }"""

dialog_replacement = """    if (showUpdateDialog && updateInfo != null) {
        val latestVersion = updateInfo!!.tag_name.removePrefix("v")
        val isNewer = latestVersion > (currentVersion ?: "0")
        var isDownloading by remember { mutableStateOf(false) }
        var downloadProgress by remember { mutableStateOf(0f) }
        
        AlertDialog(
            onDismissRequest = { if (!isDownloading) showUpdateDialog = false },
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
                        if (isDownloading) {
                            Text("Mengunduh update... ${(downloadProgress * 100).toInt()}%")
                            Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.material3.LinearProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier.fillMaxWidth(),
                                color = primaryBg,
                            )
                        } else {
                            Text("Apakah Anda ingin mengunduh versi terbaru?")
                        }
                    }
                }
            },
            confirmButton = {
                if (isNewer) {
                    TextButton(
                        onClick = {
                            val url = updateInfo!!.assets.firstOrNull()?.browser_download_url
                            if (url != null) {
                                isDownloading = true
                                coroutineScope.launch {
                                    val file = downloadApk(context, url) { progress ->
                                        downloadProgress = progress
                                    }
                                    isDownloading = false
                                    if (file != null) {
                                        showUpdateDialog = false
                                        installApk(context, file)
                                    } else {
                                        Toast.makeText(context, "Gagal mengunduh update", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Link download tidak ditemukan", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isDownloading
                    ) {
                        Text(if (isDownloading) "Mengunduh..." else "Download & Install", color = if (isDownloading) Color.Gray else primaryBg)
                    }
                } else {
                    TextButton(onClick = { showUpdateDialog = false }) {
                        Text("Tutup", color = primaryBg)
                    }
                }
            },
            dismissButton = {
                if (isNewer && !isDownloading) {
                    TextButton(onClick = { showUpdateDialog = false }) {
                        Text("Batal", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666))
                    }
                }
            }
        )
    }"""

content = content.replace(dialog_target, dialog_replacement)

with open("app/src/main/java/com/example/ui/screens/SettingScreen.kt", "w") as f:
    f.write(content)
