package com.example.scholarly

import API.TimeLogRequest
import API.TimeLogResponse
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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

        // Set up date picker
        dateTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                dateTextView.text = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            }, year, month, day)
            datePickerDialog.show()
        }

        // Set up time pickers
        timeInTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                timeInTextView.text = "$selectedHour:$selectedMinute"
            }, hour, minute, true)
            timePickerDialog.show()
        }

        timeOutTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                timeOutTextView.text = "$selectedHour:$selectedMinute"
            }, hour, minute, true)
            timePickerDialog.show()
        }

        // Set up submit button
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

    private fun submitLog(date: String, timeIn: String, timeOut: String) {
        val request = TimeLogRequest(date, timeIn, timeOut, "log")

        ApiClient.retrofit.create(APIService::class.java)
            .submitTimeLog(request)
            .enqueue(object : Callback<TimeLogResponse> {
                override fun onResponse(call: Call<TimeLogResponse>, response: Response<TimeLogResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@LogsActivity, "Log submitted successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@LogsActivity, "Failed to submit log", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<TimeLogResponse>, t: Throwable) {
                    Toast.makeText(this@LogsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}