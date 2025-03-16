package com.example.scholarly

import API.PastLogsResponse
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PastLogsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var backButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var apiService: APIService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_past_logs)

        initializeViews()
        setupClickListeners()
        apiService = ApiClient.retrofit.create(APIService::class.java)
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        fetchPastLogs()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.recyclerView)
        backButton = findViewById(R.id.backButton)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            startActivity(Intent(this, LogsActivity::class.java))
            finish()
        }
    }

    private fun fetchPastLogs() {
        val sessionToken = sharedPreferences.getString("SESSION_TOKEN", "") ?: ""
        if (sessionToken.isEmpty()) {
            Log.e("SESSION_ERROR", "No session token found")
            return
        }

        apiService.getPastDutyLogs(mapOf("Authorization" to "Bearer $sessionToken"))
            .enqueue(object : Callback<PastLogsResponse> {
                override fun onResponse(call: Call<PastLogsResponse>, response: Response<PastLogsResponse>) {
                    if (response.isSuccessful) {
                        val logs = response.body()?.logs ?: emptyList()
                        updateLogsList(logs)
                    } else {
                        Log.e("API_ERROR", "Failed to fetch logs, response code: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<PastLogsResponse>, t: Throwable) {
                    Log.e("API_ERROR", "Failed to fetch duty logs", t)
                }
            })
    }

    private fun updateLogsList(logs: List<API.PastLogEntry>) {
        recyclerView.adapter = LogsAdapter(logs)
    }
}