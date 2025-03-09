package com.example.scholarly

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private val gson = GsonBuilder()
        .setLenient()  // Allow parsing of malformed JSON
        .create()


    private const val BASE_URL = "http://192.168.1.6:80/duty-tracker/"
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
}