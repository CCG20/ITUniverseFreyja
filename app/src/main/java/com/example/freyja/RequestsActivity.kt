package com.example.freyja

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RequestsActivity : AppCompatActivity() {

    private lateinit var adapter: RequestsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_requests)

        val residentId = intent.getLongExtra("residentId", -1)
        val db = FreyjaDbHelper(this)

        val etTopic = findViewById<EditText>(R.id.etRequestTopic)
        val etBody = findViewById<EditText>(R.id.etRequestBody)
        val btnSend = findViewById<Button>(R.id.btnSendRequest)

        val rv = findViewById<RecyclerView>(R.id.rvRequests)
        val tvEmpty = findViewById<TextView>(R.id.tvRequestsEmpty)

        rv.layoutManager = LinearLayoutManager(this)
        adapter = RequestsAdapter(emptyList())
        rv.adapter = adapter

        fun refresh() {
            val items = db.getRequests(residentId)
            if (items.isEmpty()) {
                tvEmpty.visibility = View.VISIBLE
            } else {
                tvEmpty.visibility = View.GONE
            }
            adapter.update(items)
        }

        refresh()

        btnSend.setOnClickListener {
            val topic = etTopic.text.toString().trim()
            val body = etBody.text.toString().trim()

            if (topic.isEmpty() || body.isEmpty()) {
                Toast.makeText(this, "Заповніть тему і текст", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val id = db.addRequest(residentId, topic, body)
            if (id > 0) {
                Toast.makeText(this, "Звернення надіслано", Toast.LENGTH_SHORT).show()
                etTopic.setText("")
                etBody.setText("")
                refresh()
            } else {
                Toast.makeText(this, "Помилка збереження", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
