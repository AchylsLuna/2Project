package com.example.scholarly

import API.TimeLogRequest
import API.TimeLogResponse
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class LogsActivity : AppCompatActivity() {

    private lateinit var dateTextView: TextView
    private lateinit var timeInTextView: TextView
    private lateinit var timeOutTextView: TextView
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)

        dateTextView = findViewById(R.id.dateInput)
        timeInTextView = findViewById(R.id.timeIn)
        timeOutTextView = findViewById(R.id.timeOut)
        submitButton = findViewById(R.id.LogsSubmit)

        dateTextView.setOnClickListener { showDatePicker() }
        timeInTextView.setOnClickListener { showTimePicker(timeInTextView) }
        timeOutTextView.setOnClickListener { showTimePicker(timeOutTextView) }

        submitButton.setOnClickListener {
            val date = dateTextView.text.toString().trim()
            val timeIn = timeInTextView.text.toString().trim()
            val timeOut = timeOutTextView.text.toString().trim()

            if (date.isEmpty() || timeIn.isEmpty() || timeOut.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                submitLog(date, timeIn, timeOut)
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            dateTextView.text = "$selectedYear-${selectedMonth + 1}-$selectedDay"
        }, year, month, day).show()
    }

    private fun showTimePicker(textView: TextView) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            textView.text = String.format("%02d:%02d:00", selectedHour, selectedMinute)
        }, hour, minute, true).show()
    }

    private fun submitLog(date: String, timeIn: String, timeOut: String) {
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val sessionId = sharedPreferences.getString("SESSION_ID", "") ?: ""
        val studentId = sharedPreferences.getString("STUDENT_ID", "") ?: "" // ✅ Retrieve student_id

        if (sessionId.isEmpty() || studentId.isEmpty()) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = TimeLogRequest(studentId, date, date, timeIn, timeOut) // ✅ Include student_id

        ApiClient.retrofit.create(APIService::class.java)
            .submitTimeLog("PHPSESSID=$sessionId", request)
            .enqueue(object : Callback<TimeLogResponse> {
                override fun onResponse(
                    call: Call<TimeLogResponse>,
                    response: Response<TimeLogResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(
                            this@LogsActivity,
                            "Log submitted successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@LogsActivity,
                            "Failed: ${response.body()?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<TimeLogResponse>, t: Throwable) {
                    Toast.makeText(this@LogsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }
}