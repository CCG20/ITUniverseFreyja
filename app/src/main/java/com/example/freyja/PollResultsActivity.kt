package com.example.freyja

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView

class PollResultsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poll_results)

        val pollId = intent.getLongExtra("pollId", -1)
        val db = FreyjaDbHelper(this)

        val results = db.getPollResults(pollId)
        val items = results.map { "${it.optionText}: ${it.votes} голос(ів)" }

        val lv = findViewById<ListView>(R.id.lvResults)
        lv.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
    }
}
