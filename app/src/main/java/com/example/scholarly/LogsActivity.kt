package com.example.scholarly

import API.TimeLogRequest
import API.TimeLogResponse
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class LogsActivity : AppCompatActivity() {

    // UI Components
    private lateinit var dateTextView: TextView
    private lateinit var timeInTextView: TextView
    private lateinit var timeOutTextView: TextView
    private lateinit var submitButton: Button
    private lateinit var profileButton: ImageButton

    // SharedPreferences
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)

        // Initialize UI components
        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        dateTextView = findViewById(R.id.dateInput)
        timeInTextView = findViewById(R.id.timeIn)
        timeOutTextView = findViewById(R.id.timeOut)
        submitButton = findViewById(R.id.LogsSubmit)
        profileButton = findViewById(R.id.profilee)
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
    }

    private fun setupClickListeners() {
        // Date/Time Pickers
        dateTextView.setOnClickListener { showDatePicker() }
        timeInTextView.setOnClickListener { showTimePicker(timeInTextView) }
        timeOutTextView.setOnClickListener { showTimePicker(timeOutTextView) }

        // Submission Button
        submitButton.setOnClickListener {
            validateAndSubmitLog()
        }

        // Profile Navigation
        profileButton.setOnClickListener {
            navigateToProfile()
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
        TimePickerDialog(this, { _, hour, minute ->
            textView.text = "%02d:%02d:00".format(hour, minute)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    private fun validateAndSubmitLog() {
        val date = dateTextView.text.toString().trim()
        val timeIn = timeInTextView.text.toString().trim()
        val timeOut = timeOutTextView.text.toString().trim()

        when {
            date.isEmpty() || timeIn.isEmpty() || timeOut.isEmpty() ->
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            else -> submitLog(date, timeIn, timeOut)
        }
    }

    private fun submitLog(date: String, timeIn: String, timeOut: String) {
        val sessionToken = sharedPreferences.getString("SESSION_TOKEN", "") ?: ""

        if (sessionToken.isEmpty()) {
            showSessionExpiredMessage()
            return
        }

        val apiService = createApiService()
        val request = TimeLogRequest(date, timeIn, timeOut)

        apiService.submitTimeLog(mapOf("Authorization" to "Bearer $sessionToken"), request)
            .enqueue(object : Callback<TimeLogResponse> {
                override fun onResponse(call: Call<TimeLogResponse>, response: Response<TimeLogResponse>) {
                    handleSubmissionResponse(response)
                }

                override fun onFailure(call: Call<TimeLogResponse>, t: Throwable) {
                    handleNetworkError(t)
                }
            })
    }

    private fun createApiService(): APIService {
        return ApiClient.retrofit.newBuilder()
            .client(OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build()
            )
            .build()
            .create(APIService::class.java)
    }

    private fun handleSubmissionResponse(response: Response<TimeLogResponse>) {
        when {
            response.isSuccessful -> {
                response.body()?.let {
                    if (it.success) {
                        showSuccessMessage(it.message)
                    } else {
                        showErrorMessage(it.message)
                    }
                }
            }
            else -> {
                val error = response.errorBody()?.string() ?: "Unknown error"
                showErrorMessage("Submission failed: $error")
            }
        }
    }

    private fun navigateToProfile() {
        startActivity(Intent(this, ProfileActivity::class.java))
    }

    private fun showSessionExpiredMessage() {
        Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
    }

    private fun showSuccessMessage(message: String?) {
        Toast.makeText(this, message ?: "Log submitted successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun showErrorMessage(message: String?) {
        Toast.makeText(this, message ?: "An error occurred", Toast.LENGTH_SHORT).show()
    }

    private fun handleNetworkError(t: Throwable) {
        Toast.makeText(this, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
        Log.e("NETWORK_ERROR", "API call failed", t)
    }
}