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
        Log.d("LOGIN", "Attempting login with ID: $studentId") // Log before request

        ApiClient.retrofit.create(APIService::class.java)
            .loginUser(LoginRequest(studentId, password))
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    Log.d("LOGIN", "Response received: ${response.code()}") // Log response code

                    if (response.isSuccessful && response.body()?.success == true) {
                        Log.d("LOGIN", "Login successful")
                        startActivity(Intent(this@MainActivity, LogsActivity::class.java))
                        finish()
                    } else {
                        Log.e("LOGIN", "Login failed: Wrong ID or Password")
                        Toast.makeText(this@MainActivity, "Wrong ID or Password", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Log.e("LOGIN", "Login failed: ${t.message}")
                    Toast.makeText(this@MainActivity, "Login failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}