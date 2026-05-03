package com.example.freyja

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PaymentsPdfExporter {
    fun exportPaymentsPdf(
        context: Context,
        apartment: Int,
        residentName: String,
        payments: List<Payment>
    ): Uri? {

        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val fileName = "Freyja_Payments_${apartment}_${dateStr}_${System.currentTimeMillis()}.pdf"


        val pdf = PdfDocument()

        val pageWidth = 595
        val pageHeight = 842
        val marginLeft = 40f
        val marginTop = 60f

        val titlePaint = Paint().apply { textSize = 18f; isFakeBoldText = true }
        val textPaint = Paint().apply { textSize = 12f }
        val headerPaint = Paint().apply { textSize = 12f; isFakeBoldText = true }

        var pageNumber = 1
        var page = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas = page.canvas

        var y = marginTop

        fun newPage() {
            pdf.finishPage(page)
            pageNumber++
            page = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            canvas = page.canvas
            y = marginTop
        }

        fun drawHeader() {
            canvas.drawText("Історія платежів", marginLeft, y, titlePaint); y += 26f
            canvas.drawText("Квартира: $apartment", marginLeft, y, textPaint); y += 18f
            canvas.drawText("Мешканець: $residentName", marginLeft, y, textPaint); y += 18f
            canvas.drawText("Дата експорту: $dateStr", marginLeft, y, textPaint); y += 22f

            y += 8f
            canvas.drawText("Дата", marginLeft, y, headerPaint)
            canvas.drawText("Сума (грн)", marginLeft + 140f, y, headerPaint)
            canvas.drawText("Примітка", marginLeft + 260f, y, headerPaint)
            y += 10f
            y += 12f
        }

        drawHeader()

        if (payments.isEmpty()) {
            canvas.drawText("Записів немає.", marginLeft, y, textPaint)
        } else {
            for (p in payments) {
                // Якщо не влазить — нова сторінка
                if (y > pageHeight - 60f) {
                    newPage()
                    drawHeader()
                }

                val date = p.date
                val amount = "%.2f".format(p.amount)
                val note = p.note ?: ""

                // “обрізаємо” примітку, щоб не вилізала (простий варіант)
                val noteShort = if (note.length > 50) note.take(50) + "…" else note

                canvas.drawText(date, marginLeft, y, textPaint)
                canvas.drawText(amount, marginLeft + 140f, y, textPaint)
                canvas.drawText(noteShort, marginLeft + 260f, y, textPaint)
                y += 18f
            }
        }

        pdf.finishPage(page)

        // --- Save ---
        val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Freyja")
            }
            val outUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            if (outUri != null) {
                resolver.openOutputStream(outUri)?.use { os ->
                    pdf.writeTo(os)
                }
            }
            outUri
        } else {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val file = java.io.File(dir, fileName)
            java.io.FileOutputStream(file).use { fos ->
                pdf.writeTo(fos)
            }
            null
        }

        pdf.close()
        return uri
    }
}
