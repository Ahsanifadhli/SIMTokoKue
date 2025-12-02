package com.example.simtokokue

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private var productList: List<Product>,
    // Menambahkan "Listener" agar Activity tahu kapan tombol ditekan
    private var onEditClick: (Product) -> Unit,
    private var onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvCategoryPrice: TextView = itemView.findViewById(R.id.tvCategoryPrice)
        val tvStock: TextView = itemView.findViewById(R.id.tvStock)
        val btnEdit: Button = itemView.findViewById(R.id.btnEdit) // atau ImageButton jika pakai icon
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete) // atau ImageButton
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        // Format Rupiah
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val priceString = formatRupiah.format(product.price)

        holder.tvName.text = product.name
        // Karena category dihapus, kita tampilkan Harga saja
        holder.tvCategoryPrice.text = "Price : $priceString"
        holder.tvStock.text = "Stock : ${product.stock}"

        // Aksi Klik Tombol Edit
        holder.btnEdit.setOnClickListener {
            onEditClick(product) // Lapor ke Activity: "User mau edit produk ini"
        }

        // Aksi Klik Tombol Delete
        holder.btnDelete.setOnClickListener {
            onDeleteClick(product) // Lapor ke Activity: "User mau hapus produk ini"
        }
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    fun updateList(newList: List<Product>) {
        productList = newList
        notifyDataSetChanged() // Refresh layar
    }
}