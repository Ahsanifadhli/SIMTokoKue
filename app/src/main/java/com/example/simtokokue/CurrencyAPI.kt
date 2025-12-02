package com.example.simtokokue

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// 1. Model Data (Bentuk JSON dari internet)
data class ExchangeResponse(
    val base: String,
    val rates: Map<String, Double> // Kita ambil map rates-nya
)

// 2. Interface (Alamat Akhir API)
interface CurrencyService {
    @GET("latest/USD") // Endpoint untuk ambil kurs USD terbaru
    fun getExchangeRates(): Call<ExchangeResponse>
}

// 3. Config Retrofit (Mesin Penghubung)
object NetworkConfig {
    private const val BASE_URL = "https://open.er-api.com/v6/" // API Gratisan

    fun getService(): CurrencyService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(CurrencyService::class.java)
    }
}