import re

with open("app/src/main/java/com/example/ui/screens/PaymentSuccessScreen.kt", "r") as f:
    content = f.read()

# Replace footer text
content = re.sub(
    r'ReceiptRow\("Keterangan", "L U N A S", successGreen, isBold = true\)\s*DividerLine\(\)\s*ReceiptRow\("Admin By", com\.example\.ui\.data\.SettingsManager\.companyName, textMain\)\s*Spacer\(modifier = Modifier\.height\(32\.dp\)\)\s*Text\(\s*text = "Support By:\\nToko Ana, PT\.Telkom, PT\.Citra Selaras Terabit,",\s*color = textSecondary,',
    'ReceiptRow("Keterangan", com.example.ui.data.SettingsManager.invoiceFooterText, successGreen, isBold = true)\n            DividerLine()\n            ReceiptRow("Admin By", com.example.ui.data.SettingsManager.companyName, textMain)\n            \n            Spacer(modifier = Modifier.height(32.dp))\n            Text(\n                text = "Support By:\\n" + com.example.ui.data.SettingsManager.supportByText,\n                color = textSecondary,',
    content
)

# Replace Share intent
content = re.sub(
    r'ActionIconBtn\(Icons\.Default\.Share, "Bagikan", cardBg, neonCyan\) \{\s*val text = "Bukti Pembayaran Tagihan Internet\\nNama: \$\{customer\?\.name\}\\nTotal: \$formattedAmount\\nBulan: \$months\\nStatus: LUNAS"\s*val sendIntent = Intent\(\)\.apply \{\s*action = Intent\.ACTION_SEND\s*putExtra\(Intent\.EXTRA_TEXT, text\)\s*type = "text/plain"\s*\}\s*val shareIntent = Intent\.createChooser\(sendIntent, null\)\s*context\.startActivity\(shareIntent\)\s*\}',
    'ActionIconBtn(Icons.Default.Share, "Bagikan", cardBg, neonCyan) {\n                coroutineScope.launch {\n                    try {\n                        val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()\n                        val contentValues = ContentValues().apply {\n                            put(MediaStore.Images.Media.DISPLAY_NAME, "Invoice_${customer?.name}_$months.jpg")\n                            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")\n                        }\n                        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)\n                        if (uri != null) {\n                            context.contentResolver.openOutputStream(uri)?.use { out ->\n                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)\n                            }\n                            val sendIntent = Intent().apply {\n                                action = Intent.ACTION_SEND\n                                putExtra(Intent.EXTRA_STREAM, uri)\n                                type = "image/jpeg"\n                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)\n                            }\n                            val shareIntent = Intent.createChooser(sendIntent, "Bagikan Invoice")\n                            context.startActivity(shareIntent)\n                        } else {\n                            Toast.makeText(context, "Gagal menyiapkan gambar", Toast.LENGTH_SHORT).show()\n                        }\n                    } catch (e: Exception) {\n                        Toast.makeText(context, "Gagal membagikan gambar", Toast.LENGTH_SHORT).show()\n                    }\n                }\n            }',
    content
)

with open("app/src/main/java/com/example/ui/screens/PaymentSuccessScreen.kt", "w") as f:
    f.write(content)

