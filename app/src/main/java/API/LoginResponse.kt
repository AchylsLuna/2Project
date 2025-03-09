package com.example.scholarly

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user_id: Int?,
    val session_id: String?
)
