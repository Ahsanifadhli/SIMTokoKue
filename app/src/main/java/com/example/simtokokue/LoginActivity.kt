package com.example.simtokokue

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    // --- TENTUKAN USERNAME & PASSWORD ADMIN DI SINI ---
    private val ADMIN_USERNAME = "adminchiffon"
    private val ADMIN_PASSWORD = "eatyourcake"
    // -------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Menghubungkan ke file layout activity_login.xml
        setContentView(R.layout.activity_login)

        // Ambil referensi ke view dari XML
        val etUsername = findViewById<EditText>(R.id.et_username)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnSignIn = findViewById<Button>(R.id.btn_sign_in)

        // Beri aksi klik pada tombol sign in
        btnSignIn.setOnClickListener {
            // Ambil teks yang diketik oleh user
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Cek apakah username dan password kosong
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username dan Password tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Cek apakah username dan password sesuai
            if (username == ADMIN_USERNAME && password == ADMIN_PASSWORD) {
                // Jika BERHASIL:
                Toast.makeText(this, "Login Berhasil!", Toast.LENGTH_SHORT).show()

                // Pindah ke MainActivity (Dashboard)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)

                // Selesaikan (tutup) activity login ini agar tidak bisa kembali
                finish()

            } else {
                // Jika GAGAL:
                Toast.makeText(this, "Username atau Password salah.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}