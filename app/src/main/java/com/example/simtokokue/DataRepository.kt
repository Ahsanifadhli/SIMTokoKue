package com.example.simtokokue

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object DataRepository {
    // 1. DATA PRODUK
    var productList = ArrayList<Product>()

    // 2. DATA KEUANGAN
    var totalRevenue: Double = 0.0
    var totalExpense: Double = 0.0

    // 3. DATA BARU (RIWAYAT & PENGELUARAN)
    var transactionList = ArrayList<Transaction>() // <== List Riwayat
    var expenseList = ArrayList<Expense>()         // <== List Pengeluaran

    fun saveData(context: Context) {
        val sharedPreferences = context.getSharedPreferences("toko_kue_data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()

        // Simpan Produk
        editor.putString("data_produk", gson.toJson(productList))

        // Simpan Keuangan Total
        editor.putFloat("data_revenue", totalRevenue.toFloat())
        editor.putFloat("data_expense", totalExpense.toFloat())

        // Simpan Riwayat Transaksi & Pengeluaran (BARU)
        editor.putString("data_transaksi", gson.toJson(transactionList))
        editor.putString("data_pengeluaran", gson.toJson(expenseList))

        editor.apply()
    }

    fun loadData(context: Context) {
        val sharedPreferences = context.getSharedPreferences("toko_kue_data", Context.MODE_PRIVATE)
        val gson = Gson()

        // Load Keuangan
        totalRevenue = sharedPreferences.getFloat("data_revenue", 0f).toDouble()
        totalExpense = sharedPreferences.getFloat("data_expense", 0f).toDouble()

        // Load Produk
        val jsonProduk = sharedPreferences.getString("data_produk", null)
        if (jsonProduk != null) {
            val type = object : TypeToken<ArrayList<Product>>() {}.type
            productList = gson.fromJson(jsonProduk, type)
        }

        // Load Riwayat Transaksi (BARU)
        val jsonTransaksi = sharedPreferences.getString("data_transaksi", null)
        if (jsonTransaksi != null) {
            val type = object : TypeToken<ArrayList<Transaction>>() {}.type
            transactionList = gson.fromJson(jsonTransaksi, type)
        }

        // Load Pengeluaran (BARU)
        val jsonExpense = sharedPreferences.getString("data_pengeluaran", null)
        if (jsonExpense != null) {
            val type = object : TypeToken<ArrayList<Expense>>() {}.type
            expenseList = gson.fromJson(jsonExpense, type)
        }
    }
}