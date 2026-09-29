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

    // Exact 200mm x 140mm aspect ratio: 200 / 140 = 10 / 7
    // Rendered at high resolution for sharp printing & digital display
    const val INVOICE_WIDTH = 1600
    const val INVOICE_HEIGHT = 1120

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
        val bitmap = Bitmap.createBitmap(INVOICE_WIDTH, INVOICE_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Background (Pure White)
        canvas.drawColor(Color.WHITE)

        // 2. Outer Decorative Border (200mm x 140mm Frame)
        val framePaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        val innerFramePaint = Paint().apply {
            color = Color.parseColor("#F1F5F9")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            isAntiAlias = true
        }
        canvas.drawRect(24f, 24f, INVOICE_WIDTH - 24f, INVOICE_HEIGHT - 24f, framePaint)
        canvas.drawRect(28f, 28f, INVOICE_WIDTH - 28f, INVOICE_HEIGHT - 28f, innerFramePaint)

        // 3. KOP SURAT (Letterhead with Logo)
        val logoPath = SettingsManager.invoiceLogoPath
        val logoFile = logoPath?.let { File(it) }
        var logoBitmap: Bitmap? = null
        if (logoFile != null && logoFile.exists()) {
            try {
                logoBitmap = BitmapFactory.decodeFile(logoFile.absolutePath)
            } catch (_: Exception) {}
        }

        val kopLeft = 60f
        val kopTop = 45f
        val maxLogoW = 180f
        val maxLogoH = 135f
        var textStartX = kopLeft

        if (logoBitmap != null) {
            // Draw uploaded company logo scaled with preserved aspect ratio
            val scale = (maxLogoW / logoBitmap.width).coerceAtMost(maxLogoH / logoBitmap.height)
            val destW = logoBitmap.width * scale
            val destH = logoBitmap.height * scale
            val destX = kopLeft + (maxLogoW - destW) / 2f
            val destY = kopTop + (maxLogoH - destH) / 2f

            val paint = Paint().apply {
                isFilterBitmap = true
                isAntiAlias = true
            }
            canvas.drawBitmap(logoBitmap, null, RectF(destX, destY, destX + destW, destY + destH), paint)
            textStartX = kopLeft + maxLogoW + 24f
        } else {
            // Fallback: Elegant company monogram crest
            val crestRect = RectF(kopLeft, kopTop + 5f, kopLeft + 130f, kopTop + 135f)
            val crestBg = Paint().apply {
                color = Color.parseColor("#0F2C59")
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawRoundRect(crestRect, 20f, 20f, crestBg)

            val crestTextPaint = Paint().apply {
                color = Color.parseColor("#00FFFF")
                textSize = 42f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("AM", crestRect.centerX(), crestRect.centerY() + 10f, crestTextPaint)

            val crestSub = Paint().apply {
                color = Color.WHITE
                textSize = 14f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("NET", crestRect.centerX(), crestRect.centerY() + 32f, crestSub)
            textStartX = kopLeft + 155f
        }

        // Kop Texts (Beside Logo)
        val headerLines = SettingsManager.invoiceHeader.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        val companyName = if (headerLines.isNotEmpty()) headerLines[0] else SettingsManager.companyName
        val addressLine = if (headerLines.size > 1) headerLines[1] else "Jln. Raya Akbar Media • Layanan Internet Cepat & Stabil"
        val contactLine = if (headerLines.size > 2) headerLines.drop(2).joinToString(" • ") else "WhatsApp: 0812-3456-7890 • Email: cs@akbarmedia.my.id"

        val compNamePaint = Paint().apply {
            color = Color.parseColor("#0F2C59") // Deep Corporate Navy
            textSize = 36f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val compSubPaint = Paint().apply {
            color = Color.parseColor("#0284C7") // Vibrant Blue
            textSize = 18f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val compAddressPaint = Paint().apply {
            color = Color.parseColor("#475569") // Slate Gray
            textSize = 16f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        canvas.drawText(companyName, textStartX, kopTop + 36f, compNamePaint)
        canvas.drawText("PENYEDIA LAYANAN INTERNET BROADBAND & RT/RW NET BERKUALITAS", textStartX, kopTop + 68f, compSubPaint)
        canvas.drawText(addressLine, textStartX, kopTop + 96f, compAddressPaint)
        canvas.drawText(contactLine, textStartX, kopTop + 124f, compAddressPaint)

        // Kop Surat Official Dual Divider Line
        val kopLineY = kopTop + maxLogoH + 18f
        val thickLinePaint = Paint().apply {
            color = Color.parseColor("#0F2C59")
            strokeWidth = 4f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        val thinLinePaint = Paint().apply {
            color = Color.parseColor("#0F2C59")
            strokeWidth = 1.5f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        canvas.drawLine(kopLeft, kopLineY, INVOICE_WIDTH - kopLeft, kopLineY, thickLinePaint)
        canvas.drawLine(kopLeft, kopLineY + 6f, INVOICE_WIDTH - kopLeft, kopLineY + 6f, thinLinePaint)

        // 4. Invoice Title & Meta Row (Right & Left)
        val titleY = kopLineY + 44f
        val invTitlePaint = Paint().apply {
            color = Color.parseColor("#0F2C59")
            textSize = 28f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("INVOICE / FAKTUR TAGIHAN", kopLeft, titleY, invTitlePaint)

        val badgeDim = RectF(kopLeft + 410f, titleY - 26f, kopLeft + 610f, titleY + 8f)
        val badgeDimBg = Paint().apply {
            color = Color.parseColor("#F1F5F9")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val badgeDimBorder = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        val badgeDimText = Paint().apply {
            color = Color.parseColor("#475569")
            textSize = 14f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawRoundRect(badgeDim, 6f, 6f, badgeDimBg)
        canvas.drawRoundRect(badgeDim, 6f, 6f, badgeDimBorder)
        canvas.drawText("200mm x 140mm", badgeDim.centerX(), badgeDim.centerY() + 5f, badgeDimText)

        // Meta (No. Invoice, Tanggal) on the Right
        val metaLabelPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 16f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        val metaValPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 16f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }
        val currentDate = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())

        val metaRight = INVOICE_WIDTH - kopLeft
        val metaLine1 = "No. Faktur  : $invoiceNo"
        val metaLine2 = "Tgl Terbit  : $currentDate"
        val metaLine3 = "Jatuh Tempo : Tgl 10 Tiap Bulan"

        canvas.drawText(metaLine1, metaRight - 360f, titleY - 14f, metaValPaint)
        canvas.drawText(metaLine2, metaRight - 360f, titleY + 10f, metaLabelPaint)
        canvas.drawText(metaLine3, metaRight - 360f, titleY + 34f, metaLabelPaint)

        // 5. Customer & Service Details Card (2-Column Container)
        val cardTop = titleY + 46f
        val cardBottom = cardTop + 140f
        val custCardRect = RectF(kopLeft, cardTop, INVOICE_WIDTH - kopLeft, cardBottom)
        val custCardBg = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val custCardBorder = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            isAntiAlias = true
        }
        canvas.drawRoundRect(custCardRect, 12f, 12f, custCardBg)
        canvas.drawRoundRect(custCardRect, 12f, 12f, custCardBorder)

        // Vertical card divider
        canvas.drawLine(custCardRect.centerX(), cardTop + 14f, custCardRect.centerX(), cardBottom - 14f, custCardBorder)

        val sectionTitlePaint = Paint().apply {
            color = Color.parseColor("#0284C7")
            textSize = 15f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val itemLabelPaint = Paint().apply {
            color = Color.parseColor("#475569")
            textSize = 16f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        val itemValuePaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 16f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        // Left Column: Customer Data
        val cLeft = kopLeft + 20f
        canvas.drawText("DITAGIHKAN KEPADA :", cLeft, cardTop + 30f, sectionTitlePaint)
        canvas.drawText("Nama Pelanggan", cLeft, cardTop + 62f, itemLabelPaint)
        canvas.drawText(": $customerName", cLeft + 140f, cardTop + 62f, itemValuePaint)

        canvas.drawText("No. WhatsApp", cLeft, cardTop + 90f, itemLabelPaint)
        canvas.drawText(": $customerPhone", cLeft + 140f, cardTop + 90f, itemLabelPaint)

        canvas.drawText("Area / Alamat", cLeft, cardTop + 118f, itemLabelPaint)
        canvas.drawText(": $customerArea", cLeft + 140f, cardTop + 118f, itemLabelPaint)

        // Right Column: Service & Period Data
        val cRight = custCardRect.centerX() + 20f
        canvas.drawText("INFORMASI LAYANAN :", cRight, cardTop + 30f, sectionTitlePaint)
        canvas.drawText("Paket Internet", cRight, cardTop + 62f, itemLabelPaint)
        canvas.drawText(": ${packageName ?: "-"}", cRight + 140f, cardTop + 62f, itemValuePaint)

        canvas.drawText("Periode Tagihan", cRight, cardTop + 90f, itemLabelPaint)
        canvas.drawText(": $months", cRight + 140f, cardTop + 90f, itemValuePaint)

        canvas.drawText("Jenis Layanan", cRight, cardTop + 118f, itemLabelPaint)
        canvas.drawText(": Unlimited Broadband (PPPoE)", cRight + 140f, cardTop + 118f, itemLabelPaint)

        // 6. Itemized Billing Table
        val tableTop = cardBottom + 20f
        val tableWidth = INVOICE_WIDTH - (2 * kopLeft)
        val tableHeaderH = 46f
        val tableRowH = 50f
        val tableHeaderRect = RectF(kopLeft, tableTop, kopLeft + tableWidth, tableTop + tableHeaderH)

        val tableHeaderBg = Paint().apply {
            color = Color.parseColor("#0F2C59")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(tableHeaderRect, 8f, 8f, tableHeaderBg)

        val thPaint = Paint().apply {
            color = Color.WHITE
            textSize = 15f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val thCenterPaint = Paint().apply {
            color = Color.WHITE
            textSize = 15f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val thRightPaint = Paint().apply {
            color = Color.WHITE
            textSize = 15f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        // Columns X
        val colNoX = kopLeft + 35f
        val colDescX = kopLeft + 90f
        val colPeriodX = kopLeft + 950f
        val colAmountX = kopLeft + tableWidth - 30f

        val thY = tableTop + 30f
        canvas.drawText("NO", colNoX, thY, thCenterPaint)
        canvas.drawText("DESKRIPSI LAYANAN / ITEM", colDescX, thY, thPaint)
        canvas.drawText("PERIODE", colPeriodX, thY, thPaint)
        canvas.drawText("JUMLAH (RP)", colAmountX, thY, thRightPaint)

        // Table Rows
        var rowY = tableTop + tableHeaderH
        val rowBgEven = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val rowBgOdd = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val rowBorder = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        val tdText = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 16f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        val tdCenter = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 16f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val tdAmount = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 17f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        // Row 1: Langganan Internet
        val r1Rect = RectF(kopLeft, rowY, kopLeft + tableWidth, rowY + tableRowH)
        canvas.drawRect(r1Rect, rowBgEven)
        canvas.drawRect(r1Rect, rowBorder)
        canvas.drawText("1", colNoX, rowY + 32f, tdCenter)
        canvas.drawText("Iuran Langganan Internet (${packageName ?: "Reguler"})", colDescX, rowY + 32f, tdText)
        canvas.drawText(months, colPeriodX, rowY + 32f, tdText)
        canvas.drawText(totalAmount, colAmountX, rowY + 32f, tdAmount)
        rowY += tableRowH

        // Row 2: Biaya Pemeliharaan / Administrasi (Rp 0)
        val r2Rect = RectF(kopLeft, rowY, kopLeft + tableWidth, rowY + tableRowH)
        canvas.drawRect(r2Rect, rowBgOdd)
        canvas.drawRect(r2Rect, rowBorder)
        canvas.drawText("2", colNoX, rowY + 32f, tdCenter)
        canvas.drawText("Biaya Administrasi & Pemeliharaan Jaringan", colDescX, rowY + 32f, tdText)
        canvas.drawText("-", colPeriodX, rowY + 32f, tdText)
        canvas.drawText("Rp 0", colAmountX, rowY + 32f, tdAmount)
        rowY += tableRowH

        // Total Row
        val totalH = 56f
        val totalRect = RectF(kopLeft, rowY, kopLeft + tableWidth, rowY + totalH)
        val totalBg = Paint().apply {
            color = Color.parseColor("#EFF6FF")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val totalBorder = Paint().apply {
            color = Color.parseColor("#3B82F6")
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawRoundRect(totalRect, 6f, 6f, totalBg)
        canvas.drawRoundRect(totalRect, 6f, 6f, totalBorder)

        val totalTitle = Paint().apply {
            color = Color.parseColor("#0F2C59")
            textSize = 20f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val totalVal = Paint().apply {
            color = Color.parseColor("#0284C7")
            textSize = 26f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText("TOTAL PEMBAYARAN", colDescX, rowY + 36f, totalTitle)
        canvas.drawText(totalAmount, colAmountX, rowY + 38f, totalVal)

        // 7. Bottom Section (Notes, Status Stamp, and Signature)
        val bottomY = rowY + totalH + 24f

        // Notes & Terms Box (Bottom Left)
        val notesRect = RectF(kopLeft, bottomY, kopLeft + 860f, bottomY + 175f)
        val notesBg = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val notesBorder = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        canvas.drawRoundRect(notesRect, 10f, 10f, notesBg)
        canvas.drawRoundRect(notesRect, 10f, 10f, notesBorder)

        val notesTitlePaint = Paint().apply {
            color = Color.parseColor("#0F2C59")
            textSize = 15f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val notesTextPaint = Paint().apply {
            color = Color.parseColor("#475569")
            textSize = 14f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        canvas.drawText("CATATAN & CARA PEMBAYARAN :", kopLeft + 16f, bottomY + 28f, notesTitlePaint)

        var noteCurY = bottomY + 54f
        val footerLines = SettingsManager.invoiceFooterText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        if (footerLines.isNotEmpty()) {
            footerLines.take(3).forEach { fLine ->
                canvas.drawText("• $fLine", kopLeft + 16f, noteCurY, notesTextPaint)
                noteCurY += 24f
            }
        } else {
            canvas.drawText("• Pembayaran via transfer Bank/e-Wallet atau petugas kolektor resmi.", kopLeft + 16f, noteCurY, notesTextPaint)
            noteCurY += 24f
            canvas.drawText("• Bukti invoice ini adalah dokumen sah yang diterbitkan oleh sistem.", kopLeft + 16f, noteCurY, notesTextPaint)
            noteCurY += 24f
        }
        canvas.drawText("• Layanan bantuan & konfirmasi WhatsApp: 0812-3456-7890", kopLeft + 16f, noteCurY, notesTextPaint)

        // Status Badge / Stamp (Beside Notes)
        val isLunas = status.equals("LUNAS", ignoreCase = true)
        val stampRect = RectF(kopLeft + 890f, bottomY + 15f, kopLeft + 1170f, bottomY + 155f)
        val stampBg = Paint().apply {
            color = if (isLunas) Color.parseColor("#ECFDF5") else Color.parseColor("#FEF2F2")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val stampBorder = Paint().apply {
            color = if (isLunas) Color.parseColor("#059669") else Color.parseColor("#DC2626")
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        val stampHeader = Paint().apply {
            color = if (isLunas) Color.parseColor("#059669") else Color.parseColor("#DC2626")
            textSize = 24f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val stampSub = Paint().apply {
            color = if (isLunas) Color.parseColor("#047857") else Color.parseColor("#B91C1C")
            textSize = 14f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val stampDate = Paint().apply {
            color = if (isLunas) Color.parseColor("#065F46") else Color.parseColor("#991B1B")
            textSize = 12f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        canvas.drawRoundRect(stampRect, 14f, 14f, stampBg)
        canvas.drawRoundRect(stampRect, 14f, 14f, stampBorder)

        if (isLunas) {
            canvas.drawText("★ L U N A S ★", stampRect.centerX(), stampRect.centerY() - 14f, stampHeader)
            canvas.drawText("PAID IN FULL", stampRect.centerX(), stampRect.centerY() + 12f, stampSub)
            canvas.drawText(currentDate, stampRect.centerX(), stampRect.centerY() + 34f, stampDate)
        } else {
            canvas.drawText("BELUM BAYAR", stampRect.centerX(), stampRect.centerY() - 14f, stampHeader)
            canvas.drawText("UNPAID INVOICE", stampRect.centerX(), stampRect.centerY() + 12f, stampSub)
            canvas.drawText("Mohon Segera Dibayar", stampRect.centerX(), stampRect.centerY() + 34f, stampDate)
        }

        // Signature Section (Bottom Right)
        val sigX = INVOICE_WIDTH - kopLeft - 180f
        val sigLabel = Paint().apply {
            color = Color.parseColor("#475569")
            textSize = 15f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val sigComp = Paint().apply {
            color = Color.parseColor("#0F2C59")
            textSize = 16f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val sigLine = Paint().apply {
            color = Color.parseColor("#94A3B8")
            strokeWidth = 1.5f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        val sigAdmin = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 15f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val sigSub = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 13f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        canvas.drawText("Hormat Kami,", sigX, bottomY + 22f, sigLabel)
        canvas.drawText(companyName, sigX, bottomY + 44f, sigComp)

        // Line for signature
        canvas.drawLine(sigX - 110f, bottomY + 125f, sigX + 110f, bottomY + 125f, sigLine)
        canvas.drawText("( Admin Billing & Finance )", sigX, bottomY + 148f, sigAdmin)
        canvas.drawText("Dokumen Sah Komputer", sigX, bottomY + 168f, sigSub)

        // 8. Bottom Edge Notation (Standard 200mm x 140mm)
        val footerNotation = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 13f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Faktur Resmi Akbar Media Billing • Ukuran Kertas: 200mm x 140mm (Continuous / A5 Custom) • Format .PNG", INVOICE_WIDTH / 2f, INVOICE_HEIGHT - 32f, footerNotation)

        return bitmap
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
        val fileName = "Invoice_200x140_${cleanPhone}_${System.currentTimeMillis()}.png"
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
                val chooser = Intent.createChooser(sendIntent, "Kirim Invoice Tagihan 200mm x 140mm (.png)")
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
