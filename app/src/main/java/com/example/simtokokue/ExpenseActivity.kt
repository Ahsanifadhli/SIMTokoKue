package com.example.simtokokue

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import java.util.UUID

class ExpenseActivity : AppCompatActivity() {

    private lateinit var rvExpense: RecyclerView
    private lateinit var fabAdd: ExtendedFloatingActionButton
    private lateinit var btnBack: ImageView
    private lateinit var adapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense)

        rvExpense = findViewById(R.id.rvExpense)
        fabAdd = findViewById(R.id.fabAddExpense)
        btnBack = findViewById(R.id.btnBack)

        setupRecyclerView()

        fabAdd.setOnClickListener {
            showAddExpenseDialog()
        }

        btnBack.setOnClickListener {
            finish() // Kembali ke dashboard
        }
    }

    private fun setupRecyclerView() {
        // Urutkan dari yang terbaru (tanggal paling besar di atas)
        val sortedList = DataRepository.expenseList.sortedByDescending { it.date }

        adapter = ExpenseAdapter(sortedList)
        rvExpense.layoutManager = LinearLayoutManager(this)
        rvExpense.adapter = adapter
    }

    private fun showAddExpenseDialog() {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_expense, null)

        val etDesc = view.findViewById<EditText>(R.id.etDescription)
        val etAmount = view.findViewById<EditText>(R.id.etAmount)
        val spinner = view.findViewById<Spinner>(R.id.spinnerCategory)

        // Setup Kategori
        val categories = arrayOf("Bahan Baku", "Operasional", "Gaji Karyawan", "Sewa", "Lainnya")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spinner.adapter = spinnerAdapter

        builder.setView(view)
        builder.setPositiveButton("Simpan") { _, _ ->
            val desc = etDesc.text.toString()
            val amountStr = etAmount.text.toString()
            val category = spinner.selectedItem.toString()

            if (desc.isNotEmpty() && amountStr.isNotEmpty()) {
                val amount = amountStr.toDouble()

                // 1. Buat Data Expense Baru
                val newExpense = Expense(
                    id = UUID.randomUUID().toString(),
                    description = desc,
                    amount = amount,
                    category = category,
                    date = System.currentTimeMillis() // Waktu sekarang
                )

                // 2. Simpan ke Repository
                DataRepository.expenseList.add(newExpense)
                DataRepository.totalExpense += amount // Update Total Pengeluaran
                DataRepository.saveData(this)

                // 3. Refresh Layar
                setupRecyclerView()

                Toast.makeText(this, "Pengeluaran Dicatat!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Data tidak lengkap!", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Batal", null)
        builder.show()
    }
}