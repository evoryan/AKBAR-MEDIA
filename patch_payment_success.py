import re

with open("app/src/main/java/com/example/ui/screens/PaymentSuccessScreen.kt", "r") as f:
    content = f.read()

target_imports = "import androidx.compose.material3.*"
rep_imports = "import androidx.compose.material3.*\nimport androidx.compose.ui.graphics.layer.drawLayer\nimport androidx.compose.ui.graphics.rememberGraphicsLayer\nimport androidx.compose.ui.draw.drawWithContent\nimport androidx.compose.ui.graphics.asAndroidBitmap\nimport android.provider.MediaStore\nimport android.content.ContentValues\nimport android.graphics.Bitmap"

if "import androidx.compose.ui.graphics.layer.drawLayer" not in content:
    content = content.replace(target_imports, rep_imports)

target_layer = """    val currentDate = sdf.format(Date())

    Column("""

rep_layer = """    val currentDate = sdf.format(Date())
    
    val graphicsLayer = rememberGraphicsLayer()
    val coroutineScope = rememberCoroutineScope()

    Column("""

content = content.replace(target_layer, rep_layer)

target_scroll = """        // Thermal Receipt Paper
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {"""

rep_scroll = """        // Thermal Receipt Paper
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
                .drawWithContent {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }
                    drawLayer(graphicsLayer)
                }
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {"""

content = content.replace(target_scroll, rep_scroll)

target_nama = """            ReceiptRow("Nama Kepada", "Customer $customerId", neonCyan)
            DividerLine()
            ReceiptRow("Alamat", "Pakisan", textMain)
            DividerLine()
            ReceiptRow("Area", "Pakisan", textMain)"""

rep_nama = """            ReceiptRow("Nama Kepada", customer?.name ?: "-", neonCyan)
            DividerLine()
            ReceiptRow("Alamat / Area", customer?.area ?: "-", textMain)"""

content = content.replace(target_nama, rep_nama)

target_footer = """            ReceiptRow("Keterangan", "L U N A S", successGreen, isBold = true)
            DividerLine()
            ReceiptRow("Admin By", com.example.ui.data.SettingsManager.companyName, textMain)
            
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Support By:\nToko Ana, PT.Telkom, PT.Citra Selaras Terabit,",
                color = textSecondary,"""

rep_footer = """            ReceiptRow("Keterangan", com.example.ui.data.SettingsManager.invoiceFooterText, successGreen, isBold = true)
            DividerLine()
            ReceiptRow("Admin By", com.example.ui.data.SettingsManager.companyName, textMain)
            
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Support By:\n${com.example.ui.data.SettingsManager.supportByText}",
                color = textSecondary,"""

content = content.replace(target_footer, rep_footer)

target_share = """            ActionIconBtn(Icons.Default.Share, "Bagikan", cardBg, neonCyan) {
                val text = "Bukti Pembayaran Tagihan Internet\nNama: ${customer?.name}\nTotal: $formattedAmount\nBulan: $months\nStatus: LUNAS"
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, text)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
            }"""

rep_share = """            ActionIconBtn(Icons.Default.Share, "Bagikan", cardBg, neonCyan) {
                coroutineScope.launch {
                    try {
                        val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                        val contentValues = ContentValues().apply {
                            put(MediaStore.Images.Media.DISPLAY_NAME, "Invoice_${customer?.name}_$months.jpg")
                            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        }
                        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                        if (uri != null) {
                            context.contentResolver.openOutputStream(uri)?.use { out ->
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                            }
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_STREAM, uri)
                                type = "image/jpeg"
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Bagikan Invoice")
                            context.startActivity(shareIntent)
                        } else {
                            Toast.makeText(context, "Gagal menyiapkan gambar", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Gagal membagikan gambar", Toast.LENGTH_SHORT).show()
                    }
                }
            }"""

content = content.replace(target_share, rep_share)

with open("app/src/main/java/com/example/ui/screens/PaymentSuccessScreen.kt", "w") as f:
    f.write(content)
print("Patched PaymentSuccessScreen.kt")
