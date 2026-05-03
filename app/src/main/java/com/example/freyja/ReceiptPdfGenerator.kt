package com.example.freyja

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReceiptPdfGenerator {
    fun saveReceiptPdf(
        context: Context,
        apartment: Int,
        residentName: String,
        amount: Double,
        paymentType: String,   // "Борг ОСББ" / "Інші послуги"
        note: String,
        last4: String,
        dateStr: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    ): Uri? {

        val fileName = "Freyja_Receipt_${apartment}_${dateStr}_${System.currentTimeMillis()}.pdf"

        // --- Create PDF document ---
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4-ish in points
        val page = pdfDocument.startPage(pageInfo)

        val canvas = page.canvas
        val paint = Paint().apply { textSize = 14f }
        val paintBold = Paint().apply { textSize = 18f; isFakeBoldText = true }

        var y = 60f
        fun line(text: String, bold: Boolean = false) {
            canvas.drawText(text, 50f, y, if (bold) paintBold else paint)
            y += if (bold) 26f else 22f
        }

        line("Квитанція про оплату", bold = true)
        y += 10f

        line("Дата: $dateStr")
        line("Квартира: $apartment")
        line("Мешканець: $residentName")
        line("Тип оплати: $paymentType")
        line("Сума: %.2f грн".format(amount))
        line("Примітка: $note")
        line("Карта: **** $last4")

        y += 20f
        line("Згенеровано в застосунку Freyja.")

        pdfDocument.finishPage(page)

        // --- Save PDF ---
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
                    pdfDocument.writeTo(os)
                }
            }
            outUri
        } else {
            // Без permission: зберігаємо у Documents всередині папки застосунку
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val file = java.io.File(dir, fileName)
            java.io.FileOutputStream(file).use { fos ->
                pdfDocument.writeTo(fos)
            }
            null
        }

        pdfDocument.close()
        return uri
    }
}
