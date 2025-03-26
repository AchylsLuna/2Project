package com.example.scholarly

import API.PastLogEntry
import API.PastLogsResponse
import API.TimeLogRequest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class LogsActivity : AppCompatActivity() {
    private lateinit var dateTextView: TextView
    private lateinit var timeInTextView: TextView
    private lateinit var timeOutTextView: TextView
    private lateinit var totalHoursTextView: TextView
    private lateinit var submitButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var apiService: APIService
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)

        initializeViews()
        setupClickListeners()
        apiService = ApiClient.retrofit.create(APIService::class.java)
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        fetchLogsAndTotalHours()
        setupBottomNavigation()
    }

    private fun initializeViews() {
        dateTextView = findViewById(R.id.dateInput)
        timeInTextView = findViewById(R.id.timeIn)
        timeOutTextView = findViewById(R.id.timeOut)
        totalHoursTextView = findViewById(R.id.totalhours)
        submitButton = findViewById(R.id.LogsSubmit)
        recyclerView = findViewById(R.id.recyclerView)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupClickListeners() {
        dateTextView.setOnClickListener { showDatePicker() }
        timeInTextView.setOnClickListener { showTimePicker(timeInTextView) }
        timeOutTextView.setOnClickListener { showTimePicker(timeOutTextView) }
        submitButton.setOnClickListener { validateAndSubmitLog() }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_home
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_notifications -> {
                    startActivity(Intent(this, NotificationActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_logs -> {
                    startActivity(Intent(this, PastLogsActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            dateTextView.text = "%04d-%02d-%02d".format(year, month + 1, day)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showTimePicker(textView: TextView) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(this, { _, hourOfDay, minute ->
            val calendarSelected = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hourOfDay)
                set(Calendar.MINUTE, minute)
            }
            val displayFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            textView.text = displayFormat.format(calendarSelected.time)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
    }

    private fun validateAndSubmitLog() {
        val date = dateTextView.text.toString().trim()
        val timeInDisplay = timeInTextView.text.toString().trim()
        val timeOutDisplay = timeOutTextView.text.toString().trim()

        if (date.isEmpty() || timeInDisplay.isEmpty() || timeOutDisplay.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val displayFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val apiFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        val timeInFull: String
        val timeOutFull: String

        try {
            val timeInParsed = displayFormat.parse(timeInDisplay)
            val calendarIn = Calendar.getInstance().apply {
                time = timeInParsed
                val dateParts = date.split("-").map { it.toInt() }
                set(dateParts[0], dateParts[1] - 1, dateParts[2])
            }
            timeInFull = apiFormat.format(calendarIn.time)
        } catch (e: Exception) {
            Log.e("SUBMIT_ERROR", "Failed to parse time_in: $timeInDisplay", e)
            Toast.makeText(this, "Invalid Time In format", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val timeOutParsed = displayFormat.parse(timeOutDisplay)
            val calendarOut = Calendar.getInstance().apply {
                time = timeOutParsed
                val dateParts = date.split("-").map { it.toInt() }
                set(dateParts[0], dateParts[1] - 1, dateParts[2])
            }
            timeOutFull = apiFormat.format(calendarOut.time)
        } catch (e: Exception) {
            Log.e("SUBMIT_ERROR", "Failed to parse time_out: $timeOutDisplay", e)
            Toast.makeText(this, "Invalid Time Out format", Toast.LENGTH_SHORT).show()
            return
        }

        val timeInDate = apiFormat.parse(timeInFull)
        val timeOutDate = apiFormat.parse(timeOutFull)
        if (timeOutDate <= timeInDate) {
            Toast.makeText(this, "Time Out must be after Time In", Toast.LENGTH_SHORT).show()
            return
        }

        submitLog(date, timeInFull, timeOutFull)
    }

    private fun submitLog(date: String, timeIn: String, timeOut: String) {
        val sessionToken = sharedPreferences.getString("SESSION_TOKEN", "") ?: ""
        Log.d("SUBMIT_LOG", "Sending session token: $sessionToken")
        if (sessionToken.isEmpty()) {
            showSessionExpiredMessage()
            return
        }

        val request = TimeLogRequest(date, timeIn, timeOut)

        lifecycleScope.launch {
            try {
                val response = apiService.submitTimeLog(
                    mapOf("Authorization" to "Bearer $sessionToken"), request
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    showSuccessMessage("Log submitted successfully!")
                    fetchLogsAndTotalHours()
                    resetForm()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error body"
                    Log.e("SUBMIT_LOG", "Failed: ${response.code()} - $errorBody")
                    showErrorMessage("Failed to submit log. Error: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                handleNetworkError(e)
            }
        }
    }

    private fun fetchLogsAndTotalHours() {
        val sessionToken = sharedPreferences.getString("SESSION_TOKEN", "") ?: ""
        if (sessionToken.isEmpty()) {
            Log.e("SESSION_ERROR", "No session token found")
            totalHoursTextView.text = "Total Hours Rendered: 0 hrs 0 min"
            return
        }

        apiService.getPastDutyLogs(mapOf("Authorization" to "Bearer $sessionToken"))
            .enqueue(object : Callback<PastLogsResponse> {
                override fun onResponse(call: Call<PastLogsResponse>, response: Response<PastLogsResponse>) {
                    if (response.isSuccessful) {
                        val logs = response.body()?.logs ?: emptyList()
                        Log.d("PAST_LOGS", "Fetched logs: $logs")
                        if (logs.isEmpty()) {
                            Log.d("TOTAL_HOURS", "No logs found")
                            totalHoursTextView.text = "Total Hours Rendered: 0 hrs 0 min"
                            updateLogsList(emptyList())
                            return@onResponse
                        }

                        updateLogsList(logs.take(3))

                        val approvedLogs = logs.filter { it.status == "Approved" }
                        Log.d("TOTAL_HOURS", "Approved logs: $approvedLogs")
                        val totalHours = approvedLogs.sumOf { log ->
                            val hours = log.total_hours?.toDouble() ?: 0.0
                            Log.d("TOTAL_HOURS", "Log ID ${log.id}: total_hours = $hours")
                            hours
                        }
                        Log.d("TOTAL_HOURS", "Calculated total hours: $totalHours")
                        updateTotalHours(totalHours)
                    } else {
                        Log.e("API_ERROR", "Failed to fetch logs, response code: ${response.code()}, body: ${response.errorBody()?.string()}")
                        totalHoursTextView.text = "Total Hours Rendered: 0 hrs 0 min"
                        updateLogsList(emptyList())
                    }
                }
                override fun onFailure(call: Call<PastLogsResponse>, t: Throwable) {
                    Log.e("API_ERROR", "Failed to fetch duty logs", t)
                    totalHoursTextView.text = "Total Hours Rendered: 0 hrs 0 min"
                    updateLogsList(emptyList())
                }
            })
    }

    private fun updateLogsList(logs: List<PastLogEntry>) {
        recyclerView.adapter = LogsAdapter(logs)
    }

    private fun updateTotalHours(hours: Double) {
        val fullHours = hours.toInt()
        val minutes = ((hours - fullHours) * 60).toInt()
        val formattedHours = if (fullHours > 0) {
            "$fullHours hr${if (fullHours > 1) "s" else ""}" + if (minutes > 0) " $minutes min" else ""
        } else {
            if (minutes > 0) "$minutes min" else "0 hrs"
        }
        totalHoursTextView.text = "Total Hours Rendered: $formattedHours"
    }

    private fun resetForm() {
        dateTextView.text = ""
        timeInTextView.text = ""
        timeOutTextView.text = ""
    }

    private fun showSessionExpiredMessage() {
        Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
    }

    private fun handleNetworkError(e: Throwable) {
        Toast.makeText(this, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
        Log.e("NETWORK_ERROR", "API call failed", e)
    }

    private fun showSuccessMessage(message: String) {
        Toast.makeText(this, "Message: $message", Toast.LENGTH_SHORT).show()
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
    }
}