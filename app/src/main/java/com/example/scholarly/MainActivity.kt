package com.example.scholarly

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val studentIdEditText = findViewById<EditText>(R.id.username)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.signInbtn)

        loginButton.setOnClickListener {
            val studentId = studentIdEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (studentId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter Student ID and Password", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(studentId, password)
            }
        }
    }

    private fun loginUser(studentId: String, password: String) {
        val apiService = ApiClient.retrofit.create(APIService::class.java)

        apiService.loginUser(LoginRequest(studentId, password))
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody?.success == true) {
                            // Save all student details
                            val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                            with(sharedPreferences.edit()) {
                                putString("SESSION_TOKEN", responseBody.token)
                                responseBody.student?.let { student ->
                                    putString("STUDENT_ID", student.studentId)
                                    putString("STUDENT_NAME", student.name)
                                    putString("SCHOLARSHIP_TYPE", student.scholarshipType)
                                    putString("COURSE", student.course)
                                    putString("DEPARTMENT", student.department)
                                    putString("DUTY_STATUS", student.hkDutyStatus)
                                }
                                apply()
                            }
                            startActivity(Intent(this@MainActivity, LogsActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Login failed: ${responseBody?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Login failed: ${response.errorBody()?.string()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(
                        this@MainActivity,
                        "Login failed: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}