package com.example.simtokokue

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.navigation.NavigationView
import java.text.NumberFormat
import java.util.Locale


import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    // UI Dashboard Variables
    private lateinit var tvRevenue: TextView
    private lateinit var tvProfit: TextView
    private lateinit var tvExpense: TextView
    private lateinit var tvIncome: TextView

    private lateinit var tvKursDollar: TextView

    // Tombol Transaksi
    private lateinit var fabTransaction: ExtendedFloatingActionButton

    private lateinit var barChart: BarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Load Data Paling Awal (Penting agar data tidak 0 saat aplikasi dibuka)
        DataRepository.loadData(this)

        // 2. Inisialisasi UI
        drawerLayout = findViewById(R.id.drawer_layout)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        val navView = findViewById<NavigationView>(R.id.nav_view)

        // Cari ID TextView Dashboard
        tvRevenue = findViewById(R.id.tvRevenue)
        tvProfit = findViewById(R.id.tvProfit)
        tvExpense = findViewById(R.id.tvExpense)
        tvIncome = findViewById(R.id.tvIncome)

        tvKursDollar = findViewById(R.id.tvKursDollar)

        fabTransaction = findViewById(R.id.fabTransaction)

        barChart = findViewById(R.id.barChart)

        fetchCurrencyData()

        // 3. Setup Toolbar & Sidebar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.drawer_open, R.string.drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // 4. Setup Navigasi Sidebar
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> drawerLayout.closeDrawers()

                R.id.nav_products -> {
                    startActivity(Intent(this, ProductManagementActivity::class.java))
                }

                R.id.nav_transaction -> {
                    val intent = Intent(this, TransactionActivity::class.java)
                    startActivity(intent)
                }

                R.id.nav_expense -> {
                    startActivity(Intent(this, ExpenseActivity::class.java))
                }

                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                }

                R.id.nav_reports -> Toast.makeText(this, "Segera Hadir", Toast.LENGTH_SHORT).show()

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

        // 5. Setup Tombol Transaksi (Membuka Halaman Kasir)
        fabTransaction.setOnClickListener {
            val intent = Intent(this, TransactionActivity::class.java)
            startActivity(intent)
        }
    }

    // Dipanggil setiap kali halaman ini tampil (update angka real-time)
    override fun onResume() {
        super.onResume()
        updateDashboardNumbers()
        setupChart()
    }

    private fun updateDashboardNumbers() {
        // Ambil Data Uang dari Repository
        val revenue = DataRepository.totalRevenue
        val expense = DataRepository.totalExpense

        // Rumus Bisnis Sederhana
        val profit = revenue - expense
        val income = profit // Net Income

        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        tvRevenue.text = formatRupiah.format(revenue)
        tvProfit.text = formatRupiah.format(profit)
        tvExpense.text = formatRupiah.format(expense)
        tvIncome.text = formatRupiah.format(income)
    }

    private fun fetchCurrencyData() {
        NetworkConfig.getService().getExchangeRates().enqueue(object : Callback<ExchangeResponse> {
            override fun onResponse(call: Call<ExchangeResponse>, response: Response<ExchangeResponse>) {
                if (response.isSuccessful) {
                    val rates = response.body()?.rates
                    val idrRate = rates?.get("IDR") // Ambil nilai Rupiah

                    if (idrRate != null) {
                        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                        val formattedRate = formatRupiah.format(idrRate)

                        tvKursDollar.text = "Info Kurs: 1 USD = $formattedRate"
                    }
                }
            }

            override fun onFailure(call: Call<ExchangeResponse>, t: Throwable) {
                tvKursDollar.text = "Gagal memuat kurs (Cek Internet)"
            }
        })
    }

    private fun setupChart() {
        val listTransaksi = DataRepository.transactionList
        if (listTransaksi.isEmpty()) return

        // 1. Hitung Penjualan per Produk
        // Map: Nama Produk -> Jumlah Terjual
        val salesMap = HashMap<String, Int>()

        for (transaksi in listTransaksi) {
            for (item in transaksi.items) {
                val count = salesMap.getOrDefault(item.product.name, 0)
                salesMap[item.product.name] = count + item.quantity
            }
        }

        // 2. Konversi ke Format BarChart
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        var index = 0f

        for ((name, qty) in salesMap) {
            entries.add(BarEntry(index, qty.toFloat()))
            labels.add(name)
            index++
        }

        // 3. Styling Grafik
        val dataSet = BarDataSet(entries, "Terjual (Qty)")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList() // Warna-warni
        dataSet.valueTextSize = 12f

        val barData = BarData(dataSet)
        barChart.data = barData

        // Atur Label Bawah (Nama Produk)
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.granularity = 1f
        barChart.description.isEnabled = false
        barChart.animateY(1000) // Animasi naik
        barChart.invalidate() // Refresh chart
    }



    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}