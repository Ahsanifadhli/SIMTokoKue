package com.example.simtokokue

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class TransactionActivity : AppCompatActivity() {

    private lateinit var spinnerProduct: Spinner
    private lateinit var etQuantity: EditText
    private lateinit var btnAddToCart: Button
    private lateinit var tvItemEstimate: TextView
    private lateinit var rvCart: RecyclerView
    private lateinit var tvTotalTransaction: TextView
    private lateinit var btnCheckout: Button

    private val cartList = ArrayList<CartItem>()
    private lateinit var cartAdapter: CartAdapter

    // Produk yang sedang dipilih di Spinner
    private var selectedProduct: Product? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        initUI()
        setupSpinner()
        setupCartRecyclerView()
        setupAutoCalculate() // <== Fitur Auto Hitung saat ngetik

        // TOMBOL 1: Masukkan ke Keranjang
        btnAddToCart.setOnClickListener {
            addToCart()
        }

        // TOMBOL 2: Bayar (Finalisasi)
        btnCheckout.setOnClickListener {
            processCheckout()
        }
    }

    private fun initUI() {
        spinnerProduct = findViewById(R.id.spinnerProduct)
        etQuantity = findViewById(R.id.etQuantity)
        btnAddToCart = findViewById(R.id.btnAddToCart)
        tvItemEstimate = findViewById(R.id.tvItemEstimate)
        rvCart = findViewById(R.id.rvCart)
        tvTotalTransaction = findViewById(R.id.tvTotalTransaction)
        btnCheckout = findViewById(R.id.btnCheckout)
    }

    private fun setupSpinner() {
        // Ambil nama produk dari Gudang untuk ditampilkan
        val productNames = DataRepository.productList.map { "${it.name} (Stok: ${it.stock})" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, productNames)
        spinnerProduct.adapter = adapter

        spinnerProduct.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Simpan produk yang dipilih ke variabel
                selectedProduct = DataRepository.productList[position]
                // Reset input jumlah saat ganti produk
                etQuantity.setText("")
                tvItemEstimate.text = "Subtotal: Rp 0"
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupCartRecyclerView() {
        cartAdapter = CartAdapter(cartList) { itemToDelete ->
            // Logic Hapus Item dari Keranjang
            cartList.remove(itemToDelete)
            cartAdapter.notifyDataSetChanged()
            calculateGrandTotal() // Hitung ulang total
        }
        rvCart.layoutManager = LinearLayoutManager(this)
        rvCart.adapter = cartAdapter
    }

    // FITUR AUTO HITUNG (Saat ngetik angka)
    private fun setupAutoCalculate() {
        etQuantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val qtyStr = s.toString()
                if (qtyStr.isNotEmpty() && selectedProduct != null) {
                    val qty = qtyStr.toInt()
                    val subtotal = selectedProduct!!.price * qty

                    val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                    tvItemEstimate.text = "Subtotal: ${formatRupiah.format(subtotal)}"
                } else {
                    tvItemEstimate.text = "Subtotal: Rp 0"
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun addToCart() {
        val qtyStr = etQuantity.text.toString()
        if (qtyStr.isEmpty() || selectedProduct == null) return

        val qty = qtyStr.toInt()

        // 1. Validasi Stok (Cukup gak stoknya?)
        if (qty > selectedProduct!!.stock) {
            Toast.makeText(this, "Stok tidak cukup! Sisa: ${selectedProduct!!.stock}", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Tambah ke List Keranjang
        val newItem = CartItem(selectedProduct!!, qty)
        cartList.add(newItem)
        cartAdapter.notifyDataSetChanged()

        // 3. Reset Input
        etQuantity.setText("")
        calculateGrandTotal()

        Toast.makeText(this, "Masuk keranjang", Toast.LENGTH_SHORT).show()
    }

    private fun calculateGrandTotal() {
        var grandTotal = 0.0
        for (item in cartList) {
            grandTotal += item.subtotal
        }

        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        tvTotalTransaction.text = formatRupiah.format(grandTotal)
    }

    private fun processCheckout() {
        if (cartList.isEmpty()) {
            Toast.makeText(this, "Keranjang masih kosong!", Toast.LENGTH_SHORT).show()
            return
        }

        // --- PROSES PENGURANGAN STOK DI GUDANG UTAMA ---
        for (cartItem in cartList) {
            // Cari produk asli di gudang berdasarkan ID
            val index = DataRepository.productList.indexOfFirst { it.id == cartItem.product.id }

            if (index != -1) {
                val productAsli = DataRepository.productList[index]

                // Kurangi stok
                val stokBaru = productAsli.stock - cartItem.quantity
                val updatedProduct = productAsli.copy(stock = stokBaru)

                // Update Gudang
                DataRepository.productList[index] = updatedProduct

                // Tambah Pendapatan
                DataRepository.totalRevenue += cartItem.subtotal
            }
        }

        val newTransaction = Transaction(
            id = System.currentTimeMillis().toString(), // ID unik pakai waktu
            date = System.currentTimeMillis(),          // Tanggal hari ini
            items = ArrayList(cartList),                // Salin isi keranjang
            totalPrice = cartList.sumOf { it.subtotal } // Total belanja
        )

        // Masukkan ke Repository
        DataRepository.transactionList.add(0, newTransaction) // Add ke index 0 biar paling atas (terbaru)
        // ---------------------------------------

        // Simpan Permanen
        DataRepository.saveData(this)

        Toast.makeText(this, "Transaksi Berhasil Disimpan!", Toast.LENGTH_LONG).show()
        finish() // Tutup halaman kasir, balik ke dashboard
    }
}