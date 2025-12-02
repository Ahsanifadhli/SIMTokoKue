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

class ExpenseAdapter(private val expenseList: List<Expense>) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDesc: TextView = itemView.findViewById(R.id.tvDescription)
        val tvDateCat: TextView = itemView.findViewById(R.id.tvDateCategory)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val item = expenseList[position]

        // Format Rupiah
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        // Format Tanggal (Contoh: 12 Okt 2025)
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        val dateString = dateFormat.format(Date(item.date))

        holder.tvDesc.text = item.description
        holder.tvDateCat.text = "$dateString • ${item.category}"
        holder.tvAmount.text = "-${formatRupiah.format(item.amount)}"
    }

    override fun getItemCount(): Int {
        return expenseList.size
    }
}