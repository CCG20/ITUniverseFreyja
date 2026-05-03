package com.example.freyja

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import android.content.Intent

class PollDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poll_detail)

        val pollId = intent.getLongExtra("pollId", -1)
        val residentId = intent.getLongExtra("residentId", -1)
        val db = FreyjaDbHelper(this)

        val poll = db.getPolls().first { it.id == pollId }
        val options = db.getPollOptions(pollId)

        val tvTitle = findViewById<TextView>(R.id.tvPollTitle)
        val tvBody = findViewById<TextView>(R.id.tvPollBody)
        val rg = findViewById<RadioGroup>(R.id.rgOptions)
        val btn = findViewById<Button>(R.id.btnVote)

        tvTitle.text = poll.title
        tvBody.text = poll.body ?: ""

        // якщо вже голосував — показуємо результати
        if (db.hasVoted(pollId, residentId)) {
            startActivity(Intent(this, PollResultsActivity::class.java).apply {
                putExtra("pollId", pollId)
            })
            finish()
            return
        }

        options.forEach { opt ->
            val rb = RadioButton(this)
            rb.text = opt.text
            rb.tag = opt.id
            rg.addView(rb)
        }

        btn.setOnClickListener {
            val checkedId = rg.checkedRadioButtonId
            if (checkedId == -1) {
                Toast.makeText(this, "Оберіть варіант", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val rb = findViewById<RadioButton>(checkedId)
            val optionId = rb.tag as Long

            db.vote(pollId, residentId, optionId)

            startActivity(Intent(this, PollResultsActivity::class.java).apply {
                putExtra("pollId", pollId)
            })
            finish()
        }
    }
}
