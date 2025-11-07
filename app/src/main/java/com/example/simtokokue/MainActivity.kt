package com.example.simtokokue // Ganti dengan nama package Anda

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Inisialisasi komponen
        drawerLayout = findViewById(R.id.drawer_layout) // Mencari ID dari XML
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val navView = findViewById<NavigationView>(R.id.nav_view)

        // 2. Set Toolbar sebagai Action Bar
        setSupportActionBar(toolbar)
        // Sembunyikan judul default (karena kita pakai logo di tengah)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // 3. Setup ActionBarDrawerToggle (Ikon menu "hamburger" di Toolbar)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.drawer_open, // String untuk accessibility
            R.string.drawer_close  // String untuk accessibility
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // 4. Setup listener untuk item di sidebar (NavView)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    // Tampilkan pesan
                    Toast.makeText(this, "Halaman Dashboard", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_products -> {
                    // Tampilkan pesan
                    Toast.makeText(this, "Halaman Produk", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_reports -> {
                    // Tampilkan pesan
                    Toast.makeText(this, "Halaman Laporan", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_logout -> {
                    // Panggil fungsi logout
                    logoutUser()
                }
            }
            // Tutup drawer setelah item diklik
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    /**
     * Fungsi untuk logout dan kembali ke LoginActivity.
     */
    private fun logoutUser() {
        // Buat Intent untuk kembali ke LoginActivity
        val intent = Intent(this, LoginActivity::class.java)

        // Flag ini penting:
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish() // Tutup MainActivity (halaman dashboard ini)
    }

    /**
     * Fungsi ini untuk menghandle tombol 'Back' fisik di HP.
     */
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            // Jika drawer sudah tertutup, jalankan fungsi 'Back' normal
            super.onBackPressed()
        }
    }
}