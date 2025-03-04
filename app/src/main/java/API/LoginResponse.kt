package com.example.scholarly

data class LoginResponse(
    val success: Boolean,
    val user_id: Int?,
    val message: String
)
