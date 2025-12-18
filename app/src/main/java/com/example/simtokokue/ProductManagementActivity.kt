package com.example.simtokokue

import android.text.Editable
import android.text.TextWatcher

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import java.util.UUID

class ProductManagementActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var btnMenu: ImageView

    private lateinit var rvProducts: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var productAdapter: ProductAdapter


    private lateinit var etSearch: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_management)

        // 1. LOAD DATA DULU SEBELUM TAMPILKAN
        DataRepository.loadData(this)

        initUI()
        setupRecyclerView() // Setup adapter SETELAH data di-load
        setupSearch()

        fabAdd.setOnClickListener {
            showProductDialog(null)
        }
    }

    private fun initUI() {
        drawerLayout = findViewById(R.id.drawer_layout_product)
        navView = findViewById(R.id.nav_view_product)
        btnMenu = findViewById(R.id.btnOpenMenu)
        rvProducts = findViewById(R.id.rvProducts)
        fabAdd = findViewById(R.id.fabAddProduct)

        etSearch = findViewById(R.id.etSearch)

        btnMenu.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }


                R.id.nav_products -> drawerLayout.closeDrawer(GravityCompat.START)

                // TAMBAHAN BARU DI SINI JUGA
                R.id.nav_transaction -> {
                    val intent = Intent(this, TransactionActivity::class.java)
                    startActivity(intent)
                    // Opsional: finish() kalau mau menutup halaman produk
                }

                R.id.nav_logout -> {
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Setiap user mengetik, panggil fungsi filter
                filterData(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterData(query: String) {
        val filteredList = ArrayList<Product>()

        // Loop semua data di gudang (Repository)
        for (item in DataRepository.productList) {
            // Cek apakah nama produk mengandung kata yang diketik (abaikan huruf besar/kecil)
            if (item.name.lowercase().contains(query.lowercase())) {
                filteredList.add(item)
            }
        }

        // Kalau hasil pencarian kosong, kasih tau user (Opsional)
        if (filteredList.isEmpty()) {
            // Toast.makeText(this, "Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
        }

        // Update Adapter dengan list yang sudah disaring
        productAdapter.updateList(filteredList)
    }

    private fun setupRecyclerView() {
        // PENTING: Ambil data dari DataRepository.productList
        productAdapter = ProductAdapter(
            DataRepository.productList,
            onEditClick = { product ->
                showProductDialog(product)
            },
            onDeleteClick = { product ->
                showDeleteConfirmation(product)
            }
        )
        rvProducts.layoutManager = LinearLayoutManager(this)
        rvProducts.adapter = productAdapter
    }

    private fun showProductDialog(productToEdit: Product?) {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_product, null)

        val etName = view.findViewById<EditText>(R.id.etProductName)
        val etPrice = view.findViewById<EditText>(R.id.etProductPrice)
        val etStock = view.findViewById<EditText>(R.id.etProductStock)

        if (productToEdit != null) {
            etName.setText(productToEdit.name)
            etPrice.setText(productToEdit.price.toInt().toString())
            etStock.setText(productToEdit.stock.toString())
            builder.setTitle("Edit Produk")
        } else {
            builder.setTitle("Tambah Produk")
        }

        builder.setView(view)
        builder.setPositiveButton("Simpan") { _, _ ->
            val name = etName.text.toString()
            val priceStr = etPrice.text.toString()
            val stockStr = etStock.text.toString()

            if (name.isNotEmpty() && priceStr.isNotEmpty() && stockStr.isNotEmpty()) {
                val price = priceStr.toDouble()
                val stock = stockStr.toInt()

                if (productToEdit == null) {
                    // --- CREATE ---
                    val newProduct = Product(UUID.randomUUID().toString(), name, price, stock)
                    DataRepository.productList.add(newProduct)

                    // SIMPAN PERMANEN
                    DataRepository.saveData(this) // <== TAMBAHKAN INI

                    filterData(etSearch.text.toString())
                    Toast.makeText(this, "Berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                } else {
                    // --- UPDATE ---
                    val index = DataRepository.productList.indexOfFirst { it.id == productToEdit.id }
                    if (index != -1) {
                        val updatedProduct = productToEdit.copy(name = name, price = price, stock = stock)
                        DataRepository.productList[index] = updatedProduct

                        // SIMPAN PERMANEN
                        DataRepository.saveData(this) // <== TAMBAHKAN INI

                        filterData(etSearch.text.toString())
                        Toast.makeText(this, "Berhasil diedit", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Data tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Batal", null)
        builder.show()
    }

    private fun showDeleteConfirmation(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Produk")
            .setMessage("Yakin ingin menghapus ${product.name}?")
            .setPositiveButton("Hapus") { _, _ ->
                val index = DataRepository.productList.indexOf(product)
                if (index != -1) {
                    DataRepository.productList.removeAt(index)

                    // SIMPAN PERMANEN
                    DataRepository.saveData(this) // <== TAMBAHKAN INI

                    filterData(etSearch.text.toString())
                    Toast.makeText(this, "Produk dihapus", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // Fungsi loadDummyData() yang lama DIHAPUS SAJA
    // Karena sudah dihandle otomatis oleh DataRepository
}