package com.example.scholarly

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val session_token: String?,
    val student: Student?
)

data class Student(
    val id: Int,
    val student_id: String,
    val name: String,
    val email: String,
    val course: String,
    val department: String,
    val hk_duty_status: String
)

