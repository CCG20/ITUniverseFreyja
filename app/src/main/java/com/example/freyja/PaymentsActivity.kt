package com.example.freyja

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PaymentsActivity : AppCompatActivity() {

    private lateinit var db: FreyjaDbHelper
    private lateinit var adapter: PaymentsAdapter
    private var residentId: Long = -1

    private lateinit var rv: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var btnExportPdf: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payments)

        residentId = intent.getLongExtra("residentId", -1)

        db = FreyjaDbHelper(this)

        btnExportPdf = findViewById(R.id.btnExportPaymentsPdf)
        rv = findViewById(R.id.rvPayments)
        tvEmpty = findViewById(R.id.tvPaymentsEmpty)

        rv.layoutManager = LinearLayoutManager(this)
        adapter = PaymentsAdapter(emptyList())
        rv.adapter = adapter

        btnExportPdf.setOnClickListener {
            exportPdf()
        }

        refreshPayments()
    }

    override fun onResume() {
        super.onResume()
        refreshPayments()
    }

    private fun refreshPayments() {
        if (residentId <= 0) {
            tvEmpty.visibility = View.VISIBLE
            rv.visibility = View.GONE
            tvEmpty.text = "Помилка: невідомий мешканець"
            btnExportPdf.isEnabled = false
            adapter.update(emptyList())
            return
        }

        val items = db.getPayments(residentId)

        if (items.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rv.visibility = View.GONE
            tvEmpty.text = "Платежів поки немає"
            btnExportPdf.isEnabled = false
        } else {
            tvEmpty.visibility = View.GONE
            rv.visibility = View.VISIBLE
            btnExportPdf.isEnabled = true
        }

        adapter.update(items)
    }

    private fun exportPdf() {
        if (residentId <= 0) {
            Toast.makeText(this, "Помилка: невідомий мешканець", Toast.LENGTH_SHORT).show()
            return
        }

        val resident = db.getResidentById(residentId)
        if (resident == null) {
            Toast.makeText(this, "Мешканця не знайдено", Toast.LENGTH_SHORT).show()
            return
        }

        val payments = db.getPayments(residentId)

        val uri = PaymentsPdfExporter.exportPaymentsPdf(
            context = this,
            apartment = resident.apartment,
            residentName = resident.fullName,
            payments = payments
        )

        if (uri != null) {
            Toast.makeText(this, "PDF збережено: Downloads/Freyja", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "PDF збережено в Documents застосунку", Toast.LENGTH_SHORT).show()
        }
    }
}
