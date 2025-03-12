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
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.*

class PastLogsActivity : AppCompatActivity() {

    private lateinit var tableLayout: TableLayout
    private lateinit var backButton: Button
    private var studentId: String? = null

    private val apiService: APIService by lazy {
        ApiClient.retrofit.create(APIService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_past_logs)

        tableLayout = findViewById(R.id.tableLayout)
        backButton = findViewById(R.id.backButton)

        studentId = getStudentId()

        Log.d("DEBUG", "Student ID: $studentId") // ✅ Debugging null issues

        if (studentId.isNullOrEmpty()) {
            Toast.makeText(this, "Student ID not found. Please log in again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        fetchPastLogs(studentId!!) // ✅ Safe call

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun getStudentId(): String? {
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        return sharedPreferences.getString("STUDENT_ID", null)
    }

    private fun fetchPastLogs(studentId: String) {
        val request = PastLogsRequest(studentId)

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val aPIService = ApiClient.retrofit.newBuilder()
            .client(client)
            .build()
            .create(APIService::class.java)

        apiService.getPastLogs(request).enqueue(object : Callback<PastLogsResponse> {
            override fun onResponse(call: Call<PastLogsResponse>, response: Response<PastLogsResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val logs = response.body()?.logs ?: emptyList()
                    runOnUiThread { populateTable(logs) }
                } else {
                    Toast.makeText(this@PastLogsActivity, "Failed to load past logs", Toast.LENGTH_SHORT).show()
                    Log.e("API_ERROR", "Response Code: ${response.code()} Body: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<PastLogsResponse>, t: Throwable) {
                Toast.makeText(this@PastLogsActivity, "Error loading logs", Toast.LENGTH_SHORT).show()
                Log.e("API_ERROR", "Fetching past logs failed", t)
            }
        })
    }

    private fun populateTable(logs: List<PastLogEntry>) {
        runOnUiThread {
            if (tableLayout.childCount > 1) {
                tableLayout.removeViews(1, tableLayout.childCount - 1)
            }

            if (logs.isEmpty()) {
                Toast.makeText(this, "No past logs found.", Toast.LENGTH_SHORT).show()
                return@runOnUiThread
            }

            for (log in logs) {
                val tableRow = TableRow(this)

                val dateTextView = createTextView(log.date)
                val timeInTextView = createTextView(log.timeIn)
                val timeOutTextView = createTextView(log.timeOut ?: "N/A")
                val durationTextView = createTextView(log.duration ?: "N/A")
                val statusTextView = createTextView(log.status)

                tableRow.addView(dateTextView)
                tableRow.addView(timeInTextView)
                tableRow.addView(timeOutTextView)
                tableRow.addView(durationTextView)
                tableRow.addView(statusTextView)

                tableLayout.addView(tableRow)
            }
        }
    }

    private fun createTextView(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            setPadding(16, 8, 16, 8)
        }
    }
}
