package com.example.scholarly

import API.PastLogsResponse
import API.PastLogEntry
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
    private lateinit var apiService: APIService
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TimeLogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_past_logs)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish() // Go back to previous activity
        }

        apiService = ApiClient.retrofit.create(APIService::class.java)
        loadPastDutyLogs()
    }

    private fun loadPastDutyLogs() {
        val sessionToken = "your_auth_token_here"
        apiService.getPastDutyLogs(mapOf("Authorization" to "Bearer $sessionToken"))
            .enqueue(object : Callback<PastLogsResponse> {
                override fun onResponse(call: Call<PastLogsResponse>, response: Response<PastLogsResponse>) {
                    Log.d("API_RESPONSE", "Response Code: ${response.code()}")
                    Log.d("API_RESPONSE", "Raw Response: ${response.raw()}")

                    if (response.isSuccessful) {
                        val logs = response.body()?.logs?: emptyList()
                        Log.d("API_RESPONSE", "Logs Retrieved: ${logs.size}")
                        adapter = TimeLogAdapter(logs)
                        recyclerView.adapter = adapter
                    } else {
                        Log.e("API_ERROR", "Response unsuccessful: ${response.code()} ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<PastLogsResponse>, t: Throwable) {
                    Log.e("API_ERROR", "Fetching past logs failed", t)
                }
            })
    }
}
