package com.example.scholarly

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("token") val token: String,
    @SerializedName("student") val student: Student?
)


data class Student(
    @SerializedName("id") val id: Int,
    @SerializedName("student_id") val student_id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("course") val course: String,
    @SerializedName("department") val department: String,
    @SerializedName("hk_duty_status") val hkDutyStatus: String
)
