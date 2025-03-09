package com.example.scholarly

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
        Log.d("LOGIN", "Attempting login with ID: $studentId")

        ApiClient.retrofit.create(APIService::class.java)
            .loginUser(LoginRequest(studentId, password))
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    val rawResponse = response.errorBody()?.string() ?: response.body().toString()
                    Log.d("LOGIN_RAW_RESPONSE", "Raw Response: $rawResponse") // ✅ Log raw response

                    if (response.isSuccessful && response.body()?.success == true) {
                        val studentData = response.body()?.student
                        val sessionToken = response.body()?.session_token

                        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                        with(sharedPreferences.edit()) {
                            putString("STUDENT_ID", studentData?.student_id)
                            putString("SESSION_TOKEN", sessionToken)
                            apply()
                        }

                        Log.d("LOGIN", "Login successful. Student ID: ${studentData?.student_id}")
                        startActivity(Intent(this@MainActivity, LogsActivity::class.java))
                        finish()
                    } else {
                        Log.e("LOGIN", "Login failed: ${response.message()}")
                        Toast.makeText(this@MainActivity, "Wrong ID or Password", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Log.e("LOGIN", "Login failed: ${t.message}")
                    Toast.makeText(
                        this@MainActivity,
                        "Login failed: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
