package com.example.freyja

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RequestsAdapter(private var items: List<Request>) :
    RecyclerView.Adapter<RequestsAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvTopic: TextView = v.findViewById(R.id.tvReqTopic)
        val tvMeta: TextView = v.findViewById(R.id.tvReqMeta)
        val tvBody: TextView = v.findViewById(R.id.tvReqBody)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_request, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = items[position]
        holder.tvTopic.text = r.topic
        holder.tvMeta.text = "Дата: ${r.date} | Статус: ${r.status}"
        holder.tvBody.text = r.body
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<Request>) {
        items = newItems
        notifyDataSetChanged()
    }
}
