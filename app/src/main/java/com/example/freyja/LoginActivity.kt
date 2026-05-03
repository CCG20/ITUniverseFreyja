package com.example.freyja

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etApartment = findViewById<EditText>(R.id.etApartment)
        val etPin = findViewById<EditText>(R.id.etPin)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        val db = FreyjaDbHelper(this)

        btnLogin.setOnClickListener {
            val apartment = etApartment.text.toString().trim().toIntOrNull()
            val pin = etPin.text.toString().trim()

            if (apartment == null) {
                Toast.makeText(this, "Введіть номер квартири", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pin.length != 4) {
                Toast.makeText(this, "PIN має містити 4 цифри", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val resident = db.checkPin(apartment, pin)
            if (resident == null) {
                Toast.makeText(this, "Невірна квартира або PIN", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            startActivity(Intent(this, ResidentHomeActivity::class.java).apply {
                putExtra("residentId", resident.id)
            })
        }
    }
}
