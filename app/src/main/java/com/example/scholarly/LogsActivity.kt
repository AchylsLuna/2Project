package com.example.scholarly

import API.DutyLogRequest
import API.DutyLogResponse
import API.TimeLogRequest
import API.TimeLogResponse
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class LogsActivity : AppCompatActivity() {

    private lateinit var apiService: APIService
    private lateinit var tableLayout: TableLayout
    private lateinit var dateInput: EditText
    private lateinit var timeIn: EditText
    private lateinit var timeOut: EditText
    private lateinit var submitButton: Button
    private var studentId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)

        apiService = ApiClient.retrofit.create(APIService::class.java)

        tableLayout = findViewById(R.id.tableLayout)
        dateInput = findViewById(R.id.dateInput)
        timeIn = findViewById(R.id.timeIn)
        timeOut = findViewById(R.id.timeOut)
        submitButton = findViewById(R.id.LogsSubmit)

        studentId = getStudentId()
        if (studentId.isNullOrEmpty()) {
            Toast.makeText(this, "Student ID not found. Please log in again.", Toast.LENGTH_SHORT).show()
            finish() // Prevents app from crashing
            return
        }

        fetchDutyLogs(studentId!!)

        dateInput.setOnClickListener {
            val datePicker = DatePickerFragment { selectedDate -> dateInput.setText(selectedDate) }
            datePicker.show(supportFragmentManager, "datePicker")
        }

        timeIn.setOnClickListener {
            val timePicker = TimePickerFragment { selectedTime -> timeIn.setText(selectedTime) }
            timePicker.show(supportFragmentManager, "timePicker")
        }

        timeOut.setOnClickListener {
            val timePicker = TimePickerFragment { selectedTime -> timeOut.setText(selectedTime) }
            timePicker.show(supportFragmentManager, "timePicker")
        }

        submitButton.setOnClickListener {
            val date = dateInput.text.toString().trim()
            val timeInText = timeIn.text.toString().trim()
            val timeOutText = timeOut.text.toString().trim()

            if (date.isEmpty() || timeInText.isEmpty() || timeOutText.isEmpty()) {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
            } else {
                submitDutyLog(studentId!!, date, timeInText, timeOutText)
            }
        }

        findViewById<Button>(R.id.pstLogs).setOnClickListener {
            startActivity(Intent(this, PastLogsActivity::class.java))
        }
    }

    private fun getStudentId(): String? {
        val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        return sharedPreferences.getString("student_id", null)
    }

    private fun fetchDutyLogs(studentId: String) {
        apiService.getDutyLogs(DutyLogRequest(studentId)).enqueue(object : Callback<DutyLogResponse> {
            override fun onResponse(call: Call<DutyLogResponse>, response: Response<DutyLogResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    tableLayout.removeViews(1, tableLayout.childCount - 1)
                    response.body()?.logs?.forEach { log ->
                        val row = TableRow(this@LogsActivity)
                        row.addView(addTextViewToRow(log.duty_date))
                        row.addView(addTextViewToRow(log.time_in))
                        row.addView(addTextViewToRow(log.time_out))
                        row.addView(addTextViewToRow(log.duration))
                        row.addView(addTextViewToRow(log.status))
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
        apiService.submitDutyLog(TimeLogRequest(studentId, date, timeIn, timeOut))
            .enqueue(object : Callback<TimeLogResponse> {
                override fun onResponse(call: Call<TimeLogResponse>, response: Response<TimeLogResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@LogsActivity, "Log submitted successfully!", Toast.LENGTH_SHORT).show()
                        fetchDutyLogs(studentId)
                    } else {
                        Toast.makeText(this@LogsActivity, "Failed to submit log.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<TimeLogResponse>, t: Throwable) {
                    Toast.makeText(this@LogsActivity, "Submission failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun addTextViewToRow(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            setPadding(8, 8, 8, 8)
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            gravity = android.view.Gravity.CENTER
        }
    }
}

class DatePickerFragment(private val listener: (String) -> Unit) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?) = DatePickerDialog(
        requireContext(),
        { _, year, month, dayOfMonth -> listener("$year-${month + 1}-$dayOfMonth") },
        Calendar.getInstance().get(Calendar.YEAR),
        Calendar.getInstance().get(Calendar.MONTH),
        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    )
}

class TimePickerFragment(private val listener: (String) -> Unit) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?) = TimePickerDialog(
        requireContext(),
        { _, hour, minute -> listener(String.format("%02d:%02d", hour, minute)) },
        Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        Calendar.getInstance().get(Calendar.MINUTE),
        true
    )
}
