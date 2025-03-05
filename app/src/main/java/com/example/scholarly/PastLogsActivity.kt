package com.example.scholarly

import API.PastLogsRequest
import API.PastLogsResponse
import API.PastLogEntry
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PastLogsActivity : AppCompatActivity() {

    private lateinit var tableLayout: TableLayout
    private lateinit var backButton: Button
    private lateinit var apiService: APIService
    private var studentId: String? = null  // Fetched from shared preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_past_logs)

        tableLayout = findViewById(R.id.tableLayout)
        backButton = findViewById(R.id.backButton)
        apiService = ApiClient.retrofit.create(APIService::class.java)

        studentId = getStudentId()

        if (studentId != null) {
            fetchPastLogs(studentId!!)
        } else {
            Toast.makeText(this, "Student ID not found. Please log in again.", Toast.LENGTH_SHORT).show()
        }

        backButton.setOnClickListener {
            finish()  // Close activity and go back
        }
    }

    private fun getStudentId(): String? {
        val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        return sharedPreferences.getString("student_id", null)
    }

    private fun fetchPastLogs(studentId: String) {
        val request = PastLogsRequest(studentId)

        apiService.getPastLogs(request).enqueue(object : Callback<PastLogsResponse> {
            override fun onResponse(call: Call<PastLogsResponse>, response: Response<PastLogsResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val logs = response.body()?.logs ?: emptyList()
                    populateTable(logs)
                } else {
                    Toast.makeText(this@PastLogsActivity, "Failed to load past logs", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PastLogsResponse>, t: Throwable) {
                Log.e("API_ERROR", "Fetching past logs failed", t)
            }
        })
    }

    private fun populateTable(logs: List<PastLogEntry>) {
        for (log in logs) {
            val tableRow = TableRow(this)

            val dateTextView = createTextView(log.date)
            val timeInTextView = createTextView(log.timeIn)
            val timeOutTextView = createTextView(log.timeOut)
            val durationTextView = createTextView(log.duration)
            val statusTextView = createTextView(log.status)

            tableRow.addView(dateTextView)
            tableRow.addView(timeInTextView)
            tableRow.addView(timeOutTextView)
            tableRow.addView(durationTextView)
            tableRow.addView(statusTextView)

            tableLayout.addView(tableRow)
        }
    }

    private fun createTextView(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            setPadding(8, 8, 8, 8)
        }
    }
}
