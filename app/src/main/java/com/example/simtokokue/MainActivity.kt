package com.example.simtokokue

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    // UI Variables
    private lateinit var tvRevenue: TextView
    private lateinit var tvProfit: TextView
    private lateinit var tvExpense: TextView
    private lateinit var tvIncome: TextView
    private lateinit var tvKursDollar: TextView
    private lateinit var fabTransaction: ExtendedFloatingActionButton

    // UI Filter & Grafik
    private lateinit var btnFilterChart: MaterialButton
    private lateinit var tvDateRange: TextView
    private lateinit var chartProducts: BarChart
    private lateinit var chartFinance: BarChart

    // Range Tanggal (Default: Semua Waktu)
    private var startDateFilter: Long = 0
    private var endDateFilter: Long = Long.MAX_VALUE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        DataRepository.loadData(this)

        initUI()
        setupToolbarAndDrawer()
        setupNavigation()
        fetchCurrencyData()

        // Setup Tombol Transaksi
        fabTransaction.setOnClickListener {
            startActivity(Intent(this, TransactionActivity::class.java))
        }

        // Setup Filter Tanggal
        btnFilterChart.setOnClickListener {
            showDateRangePicker()
        }
    }

    private fun initUI() {
        drawerLayout = findViewById(R.id.drawer_layout)
        tvRevenue = findViewById(R.id.tvRevenue)
        tvProfit = findViewById(R.id.tvProfit)
        tvExpense = findViewById(R.id.tvExpense)
        tvIncome = findViewById(R.id.tvIncome)
        tvKursDollar = findViewById(R.id.tvKursDollar)
        fabTransaction = findViewById(R.id.fabTransaction)

        btnFilterChart = findViewById(R.id.btnFilterChart)
        tvDateRange = findViewById(R.id.tvDateRange)
        chartProducts = findViewById(R.id.chartProducts)
        chartFinance = findViewById(R.id.chartFinance)
    }

    override fun onResume() {
        super.onResume()
        updateDashboardNumbers()
        updateCharts() // Update grafik setiap kali halaman muncul
    }

    private fun updateDashboardNumbers() {
        val revenue = DataRepository.totalRevenue
        val expense = DataRepository.totalExpense
        val profit = revenue - expense
        val income = profit

        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        tvRevenue.text = formatRupiah.format(revenue)
        tvProfit.text = formatRupiah.format(profit)
        tvExpense.text = formatRupiah.format(expense)
        tvIncome.text = formatRupiah.format(income)
    }

    // --- LOGIKA FILTER TANGGAL ---
    private fun showDateRangePicker() {
        val datePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Pilih Rentang Waktu")
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            // selection.first = Start Date, selection.second = End Date
            startDateFilter = selection.first
            endDateFilter = selection.second

            // Format tanggal untuk ditampilkan
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            val startStr = sdf.format(Date(startDateFilter))
            val endStr = sdf.format(Date(endDateFilter))

            tvDateRange.text = "Menampilkan: $startStr - $endStr"

            // Refresh grafik sesuai filter
            updateCharts()
        }

        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    // --- LOGIKA GRAFIK ---
    private fun updateCharts() {
        setupProductChart()
        setupFinanceChart()
    }

    private fun setupProductChart() {
        val listTransaksi = DataRepository.transactionList

        // 1. Filter Transaksi Berdasarkan Tanggal
        val filteredList = listTransaksi.filter {
            it.date in startDateFilter..endDateFilter
        }

        // 2. Hitung Penjualan per Produk
        val salesMap = HashMap<String, Int>()
        for (trx in filteredList) {
            for (item in trx.items) {
                val count = salesMap.getOrDefault(item.product.name, 0)
                salesMap[item.product.name] = count + item.quantity
            }
        }

        // 3. Ambil TOP 3 Terlaris
        // Mengubah Map jadi List -> Sort Descending -> Ambil 3
        val topProducts = salesMap.toList()
            .sortedByDescending { (_, value) -> value }
            .take(3)

        // 4. Masukkan ke Grafik
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        topProducts.forEachIndexed { index, (name, qty) ->
            entries.add(BarEntry(index.toFloat(), qty.toFloat()))
            labels.add(name)
        }

        val dataSet = BarDataSet(entries, "Qty Terjual")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextSize = 12f

        val barData = BarData(dataSet)
        chartProducts.data = barData
        chartProducts.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chartProducts.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chartProducts.xAxis.granularity = 1f
        chartProducts.description.isEnabled = false
        chartProducts.animateY(1000)
        chartProducts.invalidate()
    }

    private fun setupFinanceChart() {
        // 1. Hitung Revenue & Expense dalam rentang tanggal

        // Revenue (Dari Transaksi)
        val filteredRevenue = DataRepository.transactionList
            .filter { it.date in startDateFilter..endDateFilter }
            .sumOf { it.totalPrice }

        // Expense (Dari Pengeluaran)
        val filteredExpense = DataRepository.expenseList
            .filter { it.date in startDateFilter..endDateFilter }
            .sumOf { it.amount }

        // 2. Masukkan ke Grafik (2 Batang: Pemasukan vs Pengeluaran)
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, filteredRevenue.toFloat()))
        entries.add(BarEntry(1f, filteredExpense.toFloat()))

        val labels = listOf("Pemasukan", "Pengeluaran")

        val dataSet = BarDataSet(entries, "Nominal (Rp)")
        // Warna: Hijau untuk Masuk, Merah untuk Keluar
        dataSet.colors = listOf(Color.parseColor("#4CAF50"), Color.parseColor("#F44336"))
        dataSet.valueTextSize = 12f

        val barData = BarData(dataSet)
        chartFinance.data = barData
        chartFinance.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chartFinance.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chartFinance.xAxis.granularity = 1f
        chartFinance.description.isEnabled = false
        chartFinance.animateY(1000)
        chartFinance.invalidate()
    }

    // --- SETUP STANDAR LAINNYA ---
    private fun setupToolbarAndDrawer() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.drawer_open, R.string.drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun setupNavigation() {
        val navView = findViewById<NavigationView>(R.id.nav_view)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> drawerLayout.closeDrawers()
                R.id.nav_products -> startActivity(Intent(this, ProductManagementActivity::class.java))
                R.id.nav_transaction -> startActivity(Intent(this, TransactionActivity::class.java))
                R.id.nav_history -> startActivity(Intent(this, HistoryActivity::class.java))
                R.id.nav_expense -> startActivity(Intent(this, ExpenseActivity::class.java))
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

    private fun fetchCurrencyData() {
        NetworkConfig.getService().getExchangeRates().enqueue(object : Callback<ExchangeResponse> {
            override fun onResponse(call: Call<ExchangeResponse>, response: Response<ExchangeResponse>) {
                if (response.isSuccessful) {
                    val rates = response.body()?.rates
                    val idrRate = rates?.get("IDR")
                    if (idrRate != null) {
                        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                        tvKursDollar.text = "Kurs: 1 USD = ${formatRupiah.format(idrRate)}"
                    }
                }
            }
            override fun onFailure(call: Call<ExchangeResponse>, t: Throwable) {
                tvKursDollar.text = "Gagal memuat kurs"
            }
        })
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}