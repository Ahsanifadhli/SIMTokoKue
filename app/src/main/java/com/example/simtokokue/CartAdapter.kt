package com.example.simtokokue

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private val cartList: ArrayList<CartItem>,
    private val onDeleteClick: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvCartName)
        val tvPrice: TextView = itemView.findViewById(R.id.tvCartPrice)
        val tvSubtotal: TextView = itemView.findViewById(R.id.tvCartSubtotal)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnRemoveCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartList[position]
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        holder.tvName.text = item.product.name
        holder.tvPrice.text = "${formatRupiah.format(item.product.price)} x ${item.quantity}"
        holder.tvSubtotal.text = formatRupiah.format(item.subtotal)

        holder.btnDelete.setOnClickListener {
            onDeleteClick(item)
        }
    }

    override fun getItemCount(): Int {
        return cartList.size
    }
}