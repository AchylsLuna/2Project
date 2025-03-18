package com.example.scholarly

import API.PastLogsResponse
import API.TimeLogRequest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var apiService: APIService
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)

        initializeViews()
        setupClickListeners()
        apiService = ApiClient.retrofit.create(APIService::class.java)

        fetchTotalHours()
        setupBottomNavigation()
    }

    private fun initializeViews() {
        dateTextView = findViewById(R.id.dateInput)
        timeInTextView = findViewById(R.id.timeIn)
        timeOutTextView = findViewById(R.id.timeOut)
        totalHoursTextView = findViewById(R.id.totalhours)
        submitButton = findViewById(R.id.LogsSubmit)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
    }

    private fun setupClickListeners() {
        dateTextView.setOnClickListener { showDatePicker() }
        timeInTextView.setOnClickListener { showTimePicker(timeInTextView) }
        timeOutTextView.setOnClickListener { showTimePicker(timeOutTextView) }
        submitButton.setOnClickListener { validateAndSubmitLog() }
        findViewById<Button>(R.id.pstLogs).setOnClickListener {
            startActivity(Intent(this, PastLogsActivity::class.java))
        }
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
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show() // Changed to false for 12-hour format
    }

    private fun validateAndSubmitLog() {
        val date = dateTextView.text.toString().trim()
        val timeInDisplay = timeInTextView.text.toString().trim()
        val timeOutDisplay = timeOutTextView.text.toString().trim()

        if (date.isEmpty() || timeInDisplay.isEmpty() || timeOutDisplay.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Convert 12-hour format with AM/PM to 24-hour format with seconds for API
        val displayFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val apiFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        val timeIn = try {
            val dateIn = displayFormat.parse(timeInDisplay)
            apiFormat.format(dateIn)
        } catch (e: Exception) {
            Log.e("TIME_PARSE_ERROR", "Failed to parse time_in: $timeInDisplay", e)
            "00:00:00" // Fallback
        }

        val timeOut = try {
            val dateOut = displayFormat.parse(timeOutDisplay)
            apiFormat.format(dateOut)
        } catch (e: Exception) {
            Log.e("TIME_PARSE_ERROR", "Failed to parse time_out: $timeOutDisplay", e)
            "00:00:00" // Fallback
        }

        submitLog(date, timeIn, timeOut)
    }

    private fun submitLog(date: String, timeIn: String, timeOut: String) {
        val sessionToken = sharedPreferences.getString("SESSION_TOKEN", "") ?: ""
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

                if (response.isSuccessful) {
                    showSuccessMessage("Log submitted successfully!")
                    fetchTotalHours()
                } else {
                    showErrorMessage("Failed to submit log. Please try again.")
                }
            } catch (e: Exception) {
                handleNetworkError(e)
            }
        }
    }

    private fun fetchTotalHours() {
        val sessionToken = sharedPreferences.getString("SESSION_TOKEN", "") ?: ""
        if (sessionToken.isEmpty()) return

        apiService.getPastDutyLogs(mapOf("Authorization" to "Bearer $sessionToken"))
            .enqueue(object : Callback<PastLogsResponse> {
                override fun onResponse(call: Call<PastLogsResponse>, response: Response<PastLogsResponse>) {
                    if (response.isSuccessful) {
                        val logs = response.body()?.logs ?: emptyList()
                        val totalHours = logs
                            .filter { it.status == "Approved" }
                            .sumOf { it.total_hours ?: 0.0 }
                        updateTotalHours(totalHours)
                    } else {
                        Log.e("API_ERROR", "Failed to fetch logs, response code: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<PastLogsResponse>, t: Throwable) {
                    Log.e("API_ERROR", "Failed to fetch duty logs", t)
                }
            })
    }

    private fun updateTotalHours(hours: Double) {
        totalHoursTextView.text = "Total Hours Rendered: %.2f".format(hours)
    }

    private fun showSessionExpiredMessage() {
        Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
    }

    private fun handleNetworkError(e: Throwable) {
        Toast.makeText(this, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
        Log.e("NETWORK_ERROR", "API call failed", e)
    }

    private fun showSuccessMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
