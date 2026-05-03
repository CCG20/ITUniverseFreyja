package com.example.freyja

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PaymentsAdapter(private var items: List<Payment>) :
    RecyclerView.Adapter<PaymentsAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvDate: TextView = v.findViewById(R.id.tvPaymentDate)
        val tvAmount: TextView = v.findViewById(R.id.tvPaymentAmount)
        val tvNote: TextView = v.findViewById(R.id.tvPaymentNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_payment, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]
        holder.tvDate.text = "Дата: ${p.date}"
        holder.tvAmount.text = "Сума: %.2f грн".format(p.amount)
        holder.tvNote.text = "Примітка: ${p.note ?: "—"}"
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<Payment>) {
        items = newItems
        notifyDataSetChanged()
    }
}
