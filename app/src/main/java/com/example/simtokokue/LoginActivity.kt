package com.example.simtokokue

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    // --- TENTUKAN USERNAME & PASSWORD ADMIN DI SINI ---
    private val ADMIN_USERNAME = "adminchiffon"
    private val ADMIN_PASSWORD = "eatyourcake"
    // -------------------------------------------------

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Menghubungkan ke file layout activity_login.xml
        setContentView(R.layout.activity_login)

        // Ambil referensi ke view dari XML
        val etUsername = findViewById<EditText>(R.id.et_username)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnSignIn = findViewById<Button>(R.id.btn_sign_in)
        val btnTogglePassword = findViewById<ImageView>(R.id.btn_toggle_password)

        // Setup toggle password visibility
        setupPasswordToggle(etPassword, btnTogglePassword)

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

    private fun setupPasswordToggle(etPassword: EditText, btnTogglePassword: ImageView) {
        btnTogglePassword.setOnClickListener {
            if (isPasswordVisible) {
                // Hide password
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                btnTogglePassword.setImageResource(R.drawable.outline_visibility_off_24)
            } else {
                // Show password
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btnTogglePassword.setImageResource(R.drawable.outline_visibility_24)
            }

            // Move cursor to end
            etPassword.setSelection(etPassword.text.length)

            // Toggle state
            isPasswordVisible = !isPasswordVisible
        }
    }
}