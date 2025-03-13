package com.example.scholarly

import API.ResetPasswordRequest
import API.ResetPasswordResponse
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val backButton = findViewById<ImageButton>(R.id.bckbutton)
        val emailEditText = findViewById<EditText>(R.id.email)
        val studentIdEditText = findViewById<EditText>(R.id.studentId)
        val newPasswordEditText = findViewById<EditText>(R.id.newpassword)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmpassword)
        val resetButton = findViewById<TextView>(R.id.resetbtn)

        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        resetButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val studentId = studentIdEditText.text.toString().trim()
            val newPassword = newPasswordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (!validateInputs(email, studentId, newPassword, confirmPassword)) {
                return@setOnClickListener
            }

            resetPassword(email, studentId, newPassword)
        }
    }

    private fun validateInputs(email: String, studentId: String, password: String, confirmPassword: String): Boolean {
        if (email.isEmpty() || studentId.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showToast("All fields are required!")
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Invalid email format!")
            return false
        }

        if (!password.matches(Regex("^[a-zA-Z0-9]{8,16}$"))) {
            showToast("Password must be 8-16 characters long and contain only letters and numbers!")
            return false
        }

        if (password != confirmPassword) {
            showToast("Passwords do not match!")
            return false
        }

        return true
    }

    private fun resetPassword(email: String, studentId: String, newPassword: String) {
        val apiService = ApiClient.createService()
        val request = ResetPasswordRequest(email, studentId, newPassword)

        apiService.resetPassword(request).enqueue(object : Callback<ResetPasswordResponse> {
            override fun onResponse(call: Call<ResetPasswordResponse>, response: Response<ResetPasswordResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.success) {
                            showToast("Password reset successfully!")
                            startActivity(Intent(this@ForgotPassword, MainActivity::class.java))
                            finish()
                        } else {
                            showToast("Failed: ${it.message}")
                        }
                    } ?: showToast("Unexpected error occurred!")
                } else {
                    showToast("Server error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResetPasswordResponse>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
