package com.example.scholarly

import com.google.gson.annotations.SerializedName

// LoginRequest.kt
data class LoginRequest(
    @SerializedName("student_id") val studentId: String,
    @SerializedName("password") val password: String
)






