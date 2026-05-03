package com.example.freyja

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class DebtActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debt)

        val residentId = intent.getLongExtra("residentId", -1)
        val db = FreyjaDbHelper(this)

        val tvDebt = findViewById<TextView>(R.id.tvDebt)
        val btnRefresh = findViewById<Button>(R.id.btnRefreshDebt)

        val btnPay = findViewById<Button>(R.id.btnOpenPayment)

        btnPay.setOnClickListener {
            startActivity(android.content.Intent(this, MakePaymentActivity::class.java).apply {
                putExtra("residentId", residentId)
            })
        }


        tvDebt.text = "Борг: 0.00 грн" // НЕ рахуємо автоматично

        btnRefresh.setOnClickListener {
            val debt = db.getResidentDebt(residentId)
            tvDebt.text = "Борг: %.2f грн".format(debt)
        }
    }
}
