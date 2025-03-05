package com.example.scholarly

import API.DutyLogRequest
import API.DutyLogResponse
import API.TimeLogRequest
import API.TimeLogResponse
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LogsActivity : AppCompatActivity() {

    private lateinit var apiService: APIService
    private lateinit var dutyLogsRecyclerView: RecyclerView
    private lateinit var dutyLogAdapter: DutyLogAdapter
    private lateinit var dateInput: EditText
    private lateinit var timeIn: EditText
    private lateinit var timeOut: EditText
    private lateinit var submitButton: Button
    private lateinit var tableLayout: TableLayout
    private var studentId: String? = null  // Dynamically fetched student ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)

        apiService = ApiClient.retrofit.create(APIService::class.java)

        dutyLogsRecyclerView = findViewById(R.id.recyclerViewDutyLogs)
        dutyLogsRecyclerView.layoutManager = LinearLayoutManager(this)
        dutyLogAdapter = DutyLogAdapter(emptyList())
        dutyLogsRecyclerView.adapter = dutyLogAdapter

        tableLayout = findViewById(R.id.tableLayout)
        dateInput = findViewById(R.id.dateInput)
        timeIn = findViewById(R.id.timeIn)
        timeOut = findViewById(R.id.timeOut)
        submitButton = findViewById(R.id.LogsSubmit)

        studentId = getStudentId()

        if (studentId != null) {
            fetchDutyLogs(studentId!!)
        } else {
            Toast.makeText(this, "Student ID not found. Please log in again.", Toast.LENGTH_SHORT).show()
        }

        // Submit button click listener
        submitButton.setOnClickListener {
            val date = dateInput.text.toString().trim()
            val timeInText = timeIn.text.toString().trim()
            val timeOutText = timeOut.text.toString().trim()

            if (date.isEmpty() || timeInText.isEmpty() || timeOutText.isEmpty()) {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
            } else {
                submitDutyLog(studentId ?: "", date, timeInText, timeOutText)
            }
        }

        // Handle Past Logs Button Click
        val pastLogsButton = findViewById<Button>(R.id.pstLogs)
        pastLogsButton.setOnClickListener {
            val intent = Intent(this, PastLogsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getStudentId(): String? {
        val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        return sharedPreferences.getString("student_id", null)
    }

    private fun fetchDutyLogs(studentId: String) {
        val request = DutyLogRequest(studentId)

        apiService.getDutyLogs(request).enqueue(object : Callback<DutyLogResponse> {
            override fun onResponse(call: Call<DutyLogResponse>, response: Response<DutyLogResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val logs = response.body()?.logs ?: emptyList()

                    // Clear previous rows (keep header)
                    tableLayout.removeViews(1, tableLayout.childCount - 1)

                    for (log in logs) {
                        val row = TableRow(this@LogsActivity)

                        val dateText = TextView(this@LogsActivity)
                        dateText.text = log.duty_date
                        dateText.setPadding(8, 8, 8, 8)
                        dateText.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)

                        val timeInText = TextView(this@LogsActivity)
                        timeInText.text = log.time_in
                        timeInText.setPadding(8, 8, 8, 8)
                        timeInText.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)

                        val timeOutText = TextView(this@LogsActivity)
                        timeOutText.text = log.time_out
                        timeOutText.setPadding(8, 8, 8, 8)
                        timeOutText.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)

                        val durationText = TextView(this@LogsActivity)
                        durationText.text = log.duration
                        durationText.setPadding(8, 8, 8, 8)
                        durationText.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)

                        val statusText = TextView(this@LogsActivity)
                        statusText.text = log.status
                        statusText.setPadding(8, 8, 8, 8)
                        statusText.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)

                        row.addView(dateText)
                        row.addView(timeInText)
                        row.addView(timeOutText)
                        row.addView(durationText)
                        row.addView(statusText)

                        tableLayout.addView(row)
                    }
                } else {
                    Toast.makeText(this@LogsActivity, "Failed to load logs", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DutyLogResponse>, t: Throwable) {
                Log.e("API_ERROR", "Fetching logs failed", t)
            }
        })
    }

    private fun submitDutyLog(studentId: String, date: String, timeIn: String, timeOut: String) {
        val request = TimeLogRequest(studentId, date, timeIn, timeOut)

        apiService.submitDutyLog(request).enqueue(object : Callback<TimeLogResponse> {
            override fun onResponse(call: Call<TimeLogResponse>, response: Response<TimeLogResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@LogsActivity, "Log submitted successfully!", Toast.LENGTH_SHORT).show()
                    fetchDutyLogs(studentId)  // Refresh logs after submission
                } else {
                    Toast.makeText(this@LogsActivity, "Failed to submit log.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TimeLogResponse>, t: Throwable) {
                Toast.makeText(this@LogsActivity, "Submission failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
