package com.example.scholarly


import API.PastLogsRequest
import API.PastLogsResponse
import API.TimeLogRequest

import API.TimeLogResponse
import Model.UploadResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body

import retrofit2.http.HeaderMap
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Part

interface APIService {
    @Headers("Content-Type: application/json")
    @POST("student/login.php")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>

    @POST("student/submit_duty_log.php")
    fun submitTimeLog(
        @HeaderMap headers: Map<String, String>,  // ✅ Accept a map for headers
        @Body request: TimeLogRequest
    ): Call<TimeLogResponse>


    @POST("student/upload_profile.php")
    fun uploadProfile(
        @HeaderMap headers: Map<String, String>,
        @Part file: MultipartBody.Part
    ): Call<UploadResponse>

    @POST("student/duty_logs.php")
    fun getPastLogs(@Body request: PastLogsRequest): Call<PastLogsResponse>

}
