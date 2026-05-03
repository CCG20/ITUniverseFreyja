package com.example.freyja

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MakePaymentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_payment)

        val residentId = intent.getLongExtra("residentId", -1)
        val db = FreyjaDbHelper(this)

        val tvCurrentDebt = findViewById<TextView>(R.id.tvCurrentDebt)

        val rgType = findViewById<RadioGroup>(R.id.rgPaymentType)
        val rbDebt = findViewById<RadioButton>(R.id.rbDebt)
        val rbService = findViewById<RadioButton>(R.id.rbService)

        val etAmount = findViewById<EditText>(R.id.etPayAmount)
        val etNote = findViewById<EditText>(R.id.etPayNote)

        // card fields
        val etCardNumber = findViewById<EditText>(R.id.etCardNumber)
        val etCardExp = findViewById<EditText>(R.id.etCardExp)
        val etCardCvv = findViewById<EditText>(R.id.etCardCvv)
        val etCardHolder = findViewById<EditText>(R.id.etCardHolder)

        val btnPay = findViewById<Button>(R.id.btnPay)

        // Показ боргу / переплати
        val debt = db.getResidentDebt(residentId)
        tvCurrentDebt.text = when {
            debt > 0 -> "Поточний борг: %.2f грн".format(debt)
            debt < 0 -> "Переплата: %.2f грн".format(-debt)
            else -> "Поточний борг: 0.00 грн"
        }

        // Борг -> примітка прихована, Послуги -> примітка показується
        etNote.visibility = View.GONE
        rgType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbDebt) {
                etNote.visibility = View.GONE
                etNote.text.clear()
            } else {
                etNote.visibility = View.VISIBLE
            }
        }

        btnPay.setOnClickListener {
            val amount = etAmount.text.toString().trim().replace(',', '.').toDoubleOrNull()
            if (amount == null || amount <= 0.0) {
                toast("Введіть коректну суму")
                return@setOnClickListener
            }

            // ---- VALIDATE CARD ----
            val cardNumberRaw = etCardNumber.text.toString().trim()
            val cardNumber = cardNumberRaw.filter { it.isDigit() }
            if (cardNumber.length < 12) { // мінімальна адекватна довжина
                toast("Введіть номер картки")
                return@setOnClickListener
            }
            if (!isValidLuhn(cardNumber)) {
                toast("Невірний номер картки")
                return@setOnClickListener
            }

            val exp = etCardExp.text.toString().trim()
            if (!isValidExp(exp)) {
                toast("Невірний термін дії (MM/YY)")
                return@setOnClickListener
            }

            val cvv = etCardCvv.text.toString().trim()
            if (!cvv.matches(Regex("^\\d{3,4}$"))) {
                toast("CVV має містити 3-4 цифри")
                return@setOnClickListener
            }

            // Не зберігаємо номер/цвв! Беремо тільки last4 для “квитанції/історії”
            val last4 = cardNumber.takeLast(4)

            val ok = if (rbDebt.isChecked) {
                val note = "Оплата боргу ОСББ (карта **** $last4)"
                db.makePayment(residentId, amount, note)
            } else {
                val noteText = etNote.text.toString().trim()
                if (noteText.isEmpty()) {
                    toast("Вкажіть, за яку послугу оплата")
                    return@setOnClickListener
                }
                val note = "$noteText (карта **** $last4)"
                addServicePayment(db, residentId, amount, note)
            }

            if (!ok) {
                toast("Оплату не виконано")
                return@setOnClickListener
            }

            toast("Оплату виконано")

// Дані для квитанції
            val resident = db.getResidentById(residentId)
            val apartment = resident?.apartment ?: 0
            val name = resident?.fullName ?: "Невідомо"
            val type = if (rbDebt.isChecked) "Борг ОСББ" else "Інші послуги"

// last4 ми вже рахували вище (cardNumber.takeLast(4))
            val uri = ReceiptPdfGenerator.saveReceiptPdf(
                context = this,
                apartment = apartment,
                residentName = name,
                amount = amount,
                paymentType = type,
                note = if (rbDebt.isChecked) "Оплата боргу ОСББ" else etNote.text.toString().trim(),
                last4 = last4
            )

            if (uri != null) {
                toast("PDF збережено в Downloads/Freyja")
            } else {
                toast("PDF збережено в Документи застосунку (Documents)")
            }

            finish()

        }
    }

    private fun addServicePayment(
        db: FreyjaDbHelper,
        residentId: Long,
        amount: Double,
        note: String
    ): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())

        val cv = android.content.ContentValues().apply {
            put("resident_id", residentId)
            put("date", today)
            put("amount", amount)
            put("note", note)
        }
        return db.writableDatabase.insert("payments", null, cv) > 0
    }

    // ---- Luhn algorithm ----
    private fun isValidLuhn(number: String): Boolean {
        var sum = 0
        var alt = false
        for (i in number.length - 1 downTo 0) {
            var n = number[i] - '0'
            if (alt) {
                n *= 2
                if (n > 9) n -= 9
            }
            sum += n
            alt = !alt
        }
        return sum % 10 == 0
    }

    // exp must be MM/YY and not expired
    private fun isValidExp(exp: String): Boolean {
        // MM/YY
        val m = Regex("^(0[1-9]|1[0-2])/(\\d{2})$").find(exp) ?: return false
        val month = m.groupValues[1].toInt()
        val year2 = m.groupValues[2].toInt()

        val cal = Calendar.getInstance()
        val currentYear2 = cal.get(Calendar.YEAR) % 100
        val currentMonth = cal.get(Calendar.MONTH) + 1

        // expire at end of month, so valid if (year>current) or (year==current and month>=currentMonth)
        return (year2 > currentYear2) || (year2 == currentYear2 && month >= currentMonth)
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
