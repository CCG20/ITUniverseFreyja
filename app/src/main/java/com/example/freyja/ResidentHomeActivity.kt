package com.example.freyja

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class ResidentHomeActivity : AppCompatActivity() {

    private lateinit var db: FreyjaDbHelper
    private var residentId: Long = -1

    private lateinit var tvName: TextView
    private lateinit var tvApartment: TextView
    private lateinit var tvEntrance: TextView
    private lateinit var tvArea: TextView
    private lateinit var tvDebt: TextView

    private lateinit var btnOpenDebt: Button
    private lateinit var btnOpenPayments: Button
    private lateinit var btnOpenMessages: Button
    private lateinit var btnOpenRequests: Button
    private lateinit var btnLogout: Button

    private lateinit var btnOpenPulls: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resident_home)

        residentId = intent.getLongExtra("residentId", -1)
        if (residentId <= 0) {
            Toast.makeText(this, "Помилка: невідомий мешканець", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        db = FreyjaDbHelper(this)

        // TextViews
        tvName = findViewById(R.id.tvResidentName)
        tvApartment = findViewById(R.id.tvResidentApartment)
        tvEntrance = findViewById(R.id.tvResidentEntrance)
        tvArea = findViewById(R.id.tvResidentArea)
        tvDebt = findViewById(R.id.tvResidentDebt)

        // Buttons
        btnOpenDebt = findViewById(R.id.btnOpenDebt)
        btnOpenPayments = findViewById(R.id.btnOpenPayments)
        btnOpenMessages = findViewById(R.id.btnOpenMessages)
        btnOpenRequests = findViewById(R.id.btnOpenRequests)
        btnLogout = findViewById(R.id.btnLogout)
        btnOpenPulls = findViewById(R.id.btnOpenPolls)

        btnOpenDebt.setOnClickListener {
            startActivity(Intent(this, DebtActivity::class.java).apply {
                putExtra("residentId", residentId)
            })
        }

        btnOpenPayments.setOnClickListener {
            startActivity(Intent(this, PaymentsActivity::class.java).apply {
                putExtra("residentId", residentId)
            })
        }

        btnOpenMessages.setOnClickListener {
            startActivity(Intent(this, MessagesActivity::class.java))
        }

        btnOpenRequests.setOnClickListener {
            startActivity(Intent(this, RequestsActivity::class.java).apply {
                putExtra("residentId", residentId)
            })
        }

        btnOpenPulls.setOnClickListener {
            startActivity(
                Intent(this, PollsActivity::class.java).apply {
                    putExtra("residentId", residentId)
                }
            )
        }

        btnLogout.setOnClickListener {
            finish() // повернення на LoginActivity
        }

        // Перше заповнення даних
        refreshResidentInfo()
    }


    override fun onResume() {
        super.onResume()
        // ВАЖЛИВО: коли повертаємось з MakePaymentActivity / DebtActivity — оновлюємо дані
        refreshResidentInfo()
    }

    private fun refreshResidentInfo() {
        val resident = db.getResidentById(residentId)
        if (resident == null) {
            Toast.makeText(this, "Мешканця не знайдено", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvName.text = "ПІБ: ${resident.fullName}"
        tvApartment.text = "Квартира: ${resident.apartment}"
        tvEntrance.text = "Під'їзд: ${resident.entrance}"
        tvArea.text = "Площа: ${resident.area} м²"

        val debt = resident.debt
        tvDebt.text = when {
            debt > 0.0 -> "Поточний борг: %.2f грн".format(debt)
            debt < 0.0 -> "Переплата: %.2f грн".format(-debt)
            else -> "Поточний борг: 0.00 грн"
        }
    }
}
