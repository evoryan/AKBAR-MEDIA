package com.example.ui.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.ui.data.SettingsManager
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object InvoiceGenerator {

    fun generateInvoiceBitmap(
        context: Context,
        customerName: String,
        customerPhone: String,
        customerArea: String,
        packageName: String?,
        months: String,
        totalAmount: String,
        status: String = "BELUM BAYAR",
        invoiceNo: String = "INV-${System.currentTimeMillis().toString().takeLast(6)}"
    ): Bitmap {
        val customPath = SettingsManager.customInvoiceTemplatePath
        val useCustom = SettingsManager.useCustomInvoiceTemplate && !customPath.isNullOrBlank()

        if (useCustom) {
            val templateFile = File(customPath!!)
            if (templateFile.exists()) {
                val loadedBitmap = BitmapFactory.decodeFile(templateFile.absolutePath)
                if (loadedBitmap != null) {
                    if (!SettingsManager.customInvoiceOverlayData) {
                        return loadedBitmap
                    }
                    return overlayDataOnCustomTemplate(
                        loadedBitmap,
                        customerName,
                        customerPhone,
                        customerArea,
                        packageName,
                        months,
                        totalAmount,
                        status,
                        invoiceNo
                    )
                }
            }
        }

        // Default: Generate crisp digital thermal invoice bitmap
        return generateDefaultThermalBitmap(
            customerName,
            customerPhone,
            customerArea,
            packageName,
            months,
            totalAmount,
            status,
            invoiceNo
        )
    }

    private fun generateDefaultThermalBitmap(
        customerName: String,
        customerPhone: String,
        customerArea: String,
        packageName: String?,
        months: String,
        totalAmount: String,
        status: String,
        invoiceNo: String
    ): Bitmap {
        val width = 720
        val height = 1100
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(Color.WHITE)

        val borderPaint = Paint().apply {
            color = Color.parseColor("#E0E0E0")
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRect(16f, 16f, width - 16f, height - 16f, borderPaint)

        val headerPaint = Paint().apply {
            color = Color.parseColor("#111111")
            textSize = 28f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.parseColor("#222222")
            textSize = 22f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            isAntiAlias = true
        }

        val boldPaint = Paint().apply {
            color = Color.parseColor("#111111")
            textSize = 22f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }

        val dashPaint = Paint().apply {
            color = Color.parseColor("#555555")
            style = Paint.Style.STROKE
            strokeWidth = 2f
            pathEffect = DashPathEffect(floatArrayOf(12f, 8f), 0f)
            isAntiAlias = true
        }

        var y = 65f

        // Company Header lines
        val headerLines = SettingsManager.invoiceHeader.split("\n")
        headerLines.forEach { line ->
            canvas.drawText(line.trim(), width / 2f, y, headerPaint)
            y += 36f
        }

        y += 10f
        drawDashedLine(canvas, 40f, y, width - 40f, y, dashPaint)
        y += 35f

        val currentDate = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale("id", "ID")).format(Date())

        drawRow(canvas, "No. Tagihan", invoiceNo, 40f, width - 40f, y, textPaint, boldPaint)
        y += 34f
        drawRow(canvas, "Tanggal", currentDate, 40f, width - 40f, y, textPaint, textPaint)
        y += 34f
        drawRow(canvas, "Pelanggan", customerName, 40f, width - 40f, y, textPaint, boldPaint)
        y += 34f
        drawRow(canvas, "No. HP", customerPhone, 40f, width - 40f, y, textPaint, textPaint)
        y += 34f
        drawRow(canvas, "Area / Lokasi", customerArea, 40f, width - 40f, y, textPaint, textPaint)
        y += 34f
        drawRow(canvas, "Paket Internet", packageName ?: "-", 40f, width - 40f, y, textPaint, textPaint)
        y += 34f
        drawRow(canvas, "Bulan Tagihan", months, 40f, width - 40f, y, textPaint, boldPaint)

        y += 20f
        drawDashedLine(canvas, 40f, y, width - 40f, y, dashPaint)
        y += 35f

        boldPaint.textSize = 24f
        canvas.drawText("RINCIAN TAGIHAN", 40f, y, boldPaint)
        boldPaint.textSize = 22f
        y += 36f

        drawRow(canvas, "Iuran Reguler", totalAmount, 40f, width - 40f, y, textPaint, textPaint)
        y += 32f
        drawRow(canvas, "Biaya Admin", "Rp. 0", 40f, width - 40f, y, textPaint, textPaint)
        y += 32f
        drawRow(canvas, "Diskon / Promo", "Rp. 0", 40f, width - 40f, y, textPaint, textPaint)

        y += 20f
        drawDashedLine(canvas, 40f, y, width - 40f, y, dashPaint)
        y += 40f

        val totalPaint = Paint().apply {
            color = Color.parseColor("#0066CC")
            textSize = 28f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }
        val totalLabelPaint = Paint().apply {
            color = Color.BLACK
            textSize = 26f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }
        drawRow(canvas, "TOTAL TAGIHAN", totalAmount, 40f, width - 40f, y, totalLabelPaint, totalPaint)

        y += 45f

        // Status Badge
        val isLunas = status.equals("LUNAS", ignoreCase = true)
        val statusBgPaint = Paint().apply {
            color = if (isLunas) Color.parseColor("#E8F5E9") else Color.parseColor("#FFEBEE")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val statusBorderPaint = Paint().apply {
            color = if (isLunas) Color.parseColor("#2E7D32") else Color.parseColor("#C62828")
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        val statusTextPaint = Paint().apply {
            color = if (isLunas) Color.parseColor("#2E7D32") else Color.parseColor("#C62828")
            textSize = 24f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val badgeRect = RectF(40f, y - 28f, width - 40f, y + 22f)
        canvas.drawRoundRect(badgeRect, 10f, 10f, statusBgPaint)
        canvas.drawRoundRect(badgeRect, 10f, 10f, statusBorderPaint)
        canvas.drawText("STATUS : $status", width / 2f, y + 8f, statusTextPaint)

        y += 50f
        drawDashedLine(canvas, 40f, y, width - 40f, y, dashPaint)
        y += 40f

        // Footer lines
        val footerPaint = Paint().apply {
            color = Color.parseColor("#444444")
            textSize = 20f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val footerLines = SettingsManager.invoiceFooterText.split("\n")
        footerLines.forEach { line ->
            canvas.drawText(line.trim(), width / 2f, y, footerPaint)
            y += 28f
        }

        return bitmap
    }

    private fun overlayDataOnCustomTemplate(
        baseBitmap: Bitmap,
        customerName: String,
        customerPhone: String,
        customerArea: String,
        packageName: String?,
        months: String,
        totalAmount: String,
        status: String,
        invoiceNo: String
    ): Bitmap {
        val result = baseBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val width = result.width.toFloat()
        val height = result.height.toFloat()

        // Responsive font scale based on bitmap resolution
        val scale = (width / 720f).coerceIn(0.8f, 3.0f)

        // Overlay Card Box (semi-white card with shadow)
        val cardMargin = 30f * scale
        val cardTop = (height * 0.42f).coerceAtMost(height - (420f * scale))
        val cardBottom = height - cardMargin
        val cardRect = RectF(cardMargin, cardTop, width - cardMargin, cardBottom)

        val cardBgPaint = Paint().apply {
            color = Color.argb(235, 255, 255, 255)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val cardBorderPaint = Paint().apply {
            color = Color.parseColor("#0077EE")
            style = Paint.Style.STROKE
            strokeWidth = 3f * scale
            isAntiAlias = true
        }
        canvas.drawRoundRect(cardRect, 16f * scale, 16f * scale, cardBgPaint)
        canvas.drawRoundRect(cardRect, 16f * scale, 16f * scale, cardBorderPaint)

        var y = cardTop + (40f * scale)
        val leftX = cardMargin + (24f * scale)
        val rightX = width - cardMargin - (24f * scale)

        val titlePaint = Paint().apply {
            color = Color.parseColor("#0A2540")
            textSize = 24f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("RINCIAN TAGIHAN INTERNET", width / 2f, y, titlePaint)
        y += 35f * scale

        val labelPaint = Paint().apply {
            color = Color.parseColor("#4A5568")
            textSize = 18f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            color = Color.parseColor("#1A202C")
            textSize = 18f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        drawRow(canvas, "No. Tagihan", invoiceNo, leftX, rightX, y, labelPaint, valuePaint)
        y += 28f * scale
        drawRow(canvas, "Nama Pelanggan", customerName, leftX, rightX, y, labelPaint, valuePaint)
        y += 28f * scale
        drawRow(canvas, "No. Handphone", customerPhone, leftX, rightX, y, labelPaint, labelPaint)
        y += 28f * scale
        drawRow(canvas, "Area / Paket", "$customerArea | ${packageName ?: "-"}", leftX, rightX, y, labelPaint, labelPaint)
        y += 28f * scale
        drawRow(canvas, "Periode Bulan", months, leftX, rightX, y, labelPaint, valuePaint)
        y += 32f * scale

        val linePaint = Paint().apply {
            color = Color.parseColor("#CBD5E0")
            strokeWidth = 1.5f * scale
            isAntiAlias = true
        }
        canvas.drawLine(leftX, y, rightX, y, linePaint)
        y += 32f * scale

        val totalTitlePaint = Paint().apply {
            color = Color.parseColor("#1A202C")
            textSize = 22f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val totalValPaint = Paint().apply {
            color = Color.parseColor("#0077EE")
            textSize = 24f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        drawRow(canvas, "TOTAL TAGIHAN", totalAmount, leftX, rightX, y, totalTitlePaint, totalValPaint)
        y += 36f * scale

        // Status badge
        val isLunas = status.equals("LUNAS", ignoreCase = true)
        val statusBg = if (isLunas) Color.parseColor("#DEF7EC") else Color.parseColor("#FDE8E8")
        val statusFg = if (isLunas) Color.parseColor("#03543F") else Color.parseColor("#9B1C1C")
        val badgeH = 34f * scale
        val bRect = RectF(leftX, y, rightX, y + badgeH)
        val bPaint = Paint().apply { color = statusBg; isAntiAlias = true }
        canvas.drawRoundRect(bRect, 8f * scale, 8f * scale, bPaint)

        val stPaint = Paint().apply {
            color = statusFg
            textSize = 18f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("STATUS : $status", width / 2f, y + (24f * scale), stPaint)

        return result
    }

    private fun drawRow(
        canvas: Canvas,
        label: String,
        value: String,
        leftX: Float,
        rightX: Float,
        y: Float,
        labelPaint: Paint,
        valPaint: Paint
    ) {
        canvas.drawText(label, leftX, y, labelPaint)
        val oldAlign = valPaint.textAlign
        valPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(value, rightX, y, valPaint)
        valPaint.textAlign = oldAlign
    }

    private fun drawDashedLine(canvas: Canvas, x1: Float, y1: Float, x2: Float, y2: Float, paint: Paint) {
        val path = Path()
        path.moveTo(x1, y1)
        path.lineTo(x2, y2)
        canvas.drawPath(path, paint)
    }

    fun generateInvoicePngFile(
        context: Context,
        customerName: String,
        customerPhone: String,
        customerArea: String,
        packageName: String?,
        months: String,
        totalAmount: String,
        status: String = "BELUM BAYAR",
        invoiceNo: String = "INV-${System.currentTimeMillis().toString().takeLast(6)}"
    ): File {
        val bitmap = generateInvoiceBitmap(
            context,
            customerName,
            customerPhone,
            customerArea,
            packageName,
            months,
            totalAmount,
            status,
            invoiceNo
        )

        val cleanPhone = customerPhone.replace(Regex("[^0-9]"), "")
        val fileName = "Tagihan_${cleanPhone}_${System.currentTimeMillis()}.png"
        val cacheDir = context.cacheDir
        val file = File(cacheDir, fileName)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
        }

        return file
    }

    fun sendWhatsappInvoiceWithPng(
        context: Context,
        phone: String,
        message: String,
        pngFile: File
    ) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pngFile
            )

            var formattedPhone = phone.replace(Regex("[^0-9]"), "")
            if (formattedPhone.startsWith("0")) {
                formattedPhone = "62" + formattedPhone.substring(1)
            }
            val jid = "$formattedPhone@s.whatsapp.net"

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, message)
                putExtra("jid", jid)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val packageManager = context.packageManager
            val isWaInstalled = try {
                packageManager.getPackageInfo("com.whatsapp", 0)
                true
            } catch (e: Exception) {
                false
            }

            val isWaBusinessInstalled = try {
                packageManager.getPackageInfo("com.whatsapp.w4b", 0)
                true
            } catch (e: Exception) {
                false
            }

            if (isWaInstalled) {
                sendIntent.setPackage("com.whatsapp")
                context.startActivity(sendIntent)
            } else if (isWaBusinessInstalled) {
                sendIntent.setPackage("com.whatsapp.w4b")
                context.startActivity(sendIntent)
            } else {
                val chooser = Intent.createChooser(sendIntent, "Kirim Invoice Tagihan (.png)")
                chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(chooser)
            }
        } catch (e: Exception) {
            // Fallback: If sending image intent fails, open text URL fallback
            try {
                var formattedPhone = phone.replace(Regex("[^0-9]"), "")
                if (formattedPhone.startsWith("0")) {
                    formattedPhone = "62" + formattedPhone.substring(1)
                }
                val url = "https://api.whatsapp.com/send?phone=$formattedPhone&text=${Uri.encode(message)}"
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(fallbackIntent)
                Toast.makeText(context, "Membuka WhatsApp teks (gambar tidak terkirim: ${e.message})", Toast.LENGTH_SHORT).show()
            } catch (e2: Exception) {
                Toast.makeText(context, "Gagal membuka WhatsApp: ${e2.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
