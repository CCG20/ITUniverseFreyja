package com.example.freyja

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView

class PollsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_polls)

        val residentId = intent.getLongExtra("residentId", -1)
        val db = FreyjaDbHelper(this)

        val polls = db.getPolls()
        val titles = polls.map { it.title }

        val lv = findViewById<ListView>(R.id.lvPolls)
        lv.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, titles)

        lv.setOnItemClickListener { _, _, position, _ ->
            startActivity(Intent(this, PollDetailActivity::class.java).apply {
                putExtra("pollId", polls[position].id)
                putExtra("residentId", residentId)
            })
        }
    }
}
