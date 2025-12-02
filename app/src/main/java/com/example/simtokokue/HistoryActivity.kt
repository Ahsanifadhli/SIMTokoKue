package com.example.simtokokue

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar

class HistoryActivity : AppCompatActivity() {

    private lateinit var rvHistory: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private lateinit var btnBack: ImageView

    // Tombol Filter
    private lateinit var btnNewest: Button
    private lateinit var btnOldest: Button
    private lateinit var btnDate: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        rvHistory = findViewById(R.id.rvHistory)
        btnBack = findViewById(R.id.btnBack)
        btnNewest = findViewById(R.id.btnFilterNewest)
        btnOldest = findViewById(R.id.btnFilterOldest)
        btnDate = findViewById(R.id.btnFilterDate)

        setupRecyclerView()

        btnBack.setOnClickListener { finish() }

        // Filter: Terbaru
        btnNewest.setOnClickListener {
            val sortedList = DataRepository.transactionList.sortedByDescending { it.date }
            adapter.updateData(sortedList)
        }

        // Filter: Terlama
        btnOldest.setOnClickListener {
            val sortedList = DataRepository.transactionList.sortedBy { it.date }
            adapter.updateData(sortedList)
        }

        // Filter: Tanggal (DatePicker)
        btnDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun setupRecyclerView() {
        // Default: Tampilkan dari yang terbaru
        val defaultList = DataRepository.transactionList.sortedByDescending { it.date }
        adapter = HistoryAdapter(defaultList)
        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = adapter
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            // Filter berdasarkan tanggal yang dipilih
            filterByDate(selectedYear, selectedMonth, selectedDay)
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun filterByDate(year: Int, month: Int, day: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, 0, 0, 0)
        val startOfDay = calendar.timeInMillis

        calendar.set(year, month, day, 23, 59, 59)
        val endOfDay = calendar.timeInMillis

        // Cari transaksi di antara jam 00:00 s/d 23:59 pada tanggal tersebut
        val filteredList = DataRepository.transactionList.filter {
            it.date in startOfDay..endOfDay
        }

        adapter.updateData(filteredList)
    }
}