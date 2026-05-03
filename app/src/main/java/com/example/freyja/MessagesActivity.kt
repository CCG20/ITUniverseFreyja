package com.example.freyja

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MessagesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        val db = FreyjaDbHelper(this)

        val rv = findViewById<RecyclerView>(R.id.rvMessages)
        val tvEmpty = findViewById<TextView>(R.id.tvMessagesEmpty)

        rv.layoutManager = LinearLayoutManager(this)
        val adapter = MessagesAdapter(emptyList())
        rv.adapter = adapter

        val items = db.getMessages()
        if (items.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
        } else {
            tvEmpty.visibility = View.GONE
            adapter.update(items)
        }
    }
}
