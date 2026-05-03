package com.example.freyja

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessagesAdapter(private var items: List<Message>) :
    RecyclerView.Adapter<MessagesAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvTitle: TextView = v.findViewById(R.id.tvMsgTitle)
        val tvDate: TextView = v.findViewById(R.id.tvMsgDate)
        val tvBody: TextView = v.findViewById(R.id.tvMsgBody)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val m = items[position]
        holder.tvTitle.text = m.title
        holder.tvDate.text = "Дата: ${m.date}"
        holder.tvBody.text = m.body
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<Message>) {
        items = newItems
        notifyDataSetChanged()
    }
}
