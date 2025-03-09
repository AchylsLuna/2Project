package com.example.scholarly

import API.TimeLogRequest
import API.TimeLogResponse
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class LogsActivity : AppCompatActivity() {
    private lateinit var dateInput: TextView
    private lateinit var timeInInput: TextView
    private lateinit var timeOutInput: TextView
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)

        dateInput = findViewById(R.id.dateInput)
        timeInInput = findViewById(R.id.timeIn)
        timeOutInput = findViewById(R.id.timeOut)  // ✅ Ensure this ID exists in XML
        submitButton = findViewById(R.id.LogsSubmit)

        dateInput.setOnClickListener { showDatePicker() }
        timeInInput.setOnClickListener { showTimePicker(timeInInput) }
        timeOutInput.setOnClickListener { showTimePicker(timeOutInput) }

        submitButton.setOnClickListener {
            val dutyDate = dateInput.text.toString().trim()
            val timeIn = timeInInput.text.toString().trim()
            val timeOut = timeOutInput.text.toString().trim()

            if (dutyDate.isEmpty() || timeIn.isEmpty() || timeOut.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            } else {
                submitDutyLog(dutyDate, timeIn, timeOut)
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog =
            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val date = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                dateInput.text = date
            }, year, month, day)

        datePickerDialog.show()
    }

    private fun showTimePicker(target: TextView) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val time = String.format("%02d:%02d", selectedHour, selectedMinute)
            target.text = time
        }, hour, minute, true)

        timePickerDialog.show()
    }

    private fun submitDutyLog(dutyDate: String, timeIn: String, timeOut: String) {
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val sessionToken = sharedPreferences.getString("SESSION_TOKEN", "") ?: ""
        val studentId =
            sharedPreferences.getString("STUDENT_ID", "") ?: ""  // ✅ Ensure student_id is included

        val apiService = ApiClient.retrofit.create(APIService::class.java)
        val request =
            TimeLogRequest(studentId, dutyDate, dutyDate, timeIn, timeOut) // ✅ Passing all values

        apiService.submitTimeLog("Bearer $sessionToken", request)
            .enqueue(object : Callback<TimeLogResponse> {
                override fun onResponse(
                    call: Call<TimeLogResponse>,
                    response: Response<TimeLogResponse>
                ) {
                    Log.d(
                        "SUBMIT_LOG_RAW_RESPONSE",
                        "Raw Response: ${
                            response.errorBody()?.string() ?: response.body().toString()
                        }"
                    )

                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(
                            this@LogsActivity,
                            "Duty log submitted successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Log.e("SUBMIT_LOG", "Error: ${response.message()}")
                        Toast.makeText(
                            this@LogsActivity,
                            "Failed to submit duty log",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<TimeLogResponse>, t: Throwable) {
                    Log.e("SUBMIT_LOG", "Submission failed: ${t.message}")
                    Toast.makeText(
                        this@LogsActivity,
                        "Submission failed: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}