package com.example.simtokokue

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(private var transactionList: List<Transaction>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvItems: TextView = itemView.findViewById(R.id.tvItems)
        val tvTotal: TextView = itemView.findViewById(R.id.tvTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val transaction = transactionList[position]

        // Format Tanggal
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        val dateString = sdf.format(Date(transaction.date))

        // Format Uang
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val totalString = formatRupiah.format(transaction.totalPrice)

        // Format Items (Gabungkan nama barang)
        // Contoh: "2x Risol, 1x Bolu"
        val itemNames = transaction.items.joinToString(", ") { "${it.quantity}x ${it.product.name}" }

        holder.tvDate.text = dateString
        holder.tvItems.text = itemNames
        holder.tvTotal.text = "+$totalString"
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }

    // Fungsi untuk update data (untuk filter)
    fun updateData(newList: List<Transaction>) {
        transactionList = newList
        notifyDataSetChanged()
    }
}