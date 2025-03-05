package com.example.scholarly

import API.DutyLogRequest
import API.DutyLogResponse
import API.PastLogsRequest
import API.PastLogsResponse
import API.TimeLogResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface APIService {
    @Headers("Content-Type: application/json")
    @POST("student/login.php")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>

    @POST("student/duty_logs.php")
    fun getDutyLogs(@Body request: DutyLogRequest): Call<DutyLogResponse>

    @POST("student/submit_duty_log.php")
    fun submitDutyLog(@Body request: API.TimeLogRequest): Call<TimeLogResponse>

    @POST("getPastLogs")
    fun getPastLogs(@Body request: PastLogsRequest): Call<PastLogsResponse>




}
