package com.example.scholarly

import API.DutyLogItem
import API.DutyLogRequest
import API.DutyLogResponse
import API.PastLogsRequest
import API.PastLogsResponse
import API.TimeLogRequest

import API.TimeLogResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface APIService {
    @Headers("Content-Type: application/json")
    @POST("student/login.php")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>

    @POST("student/duty_logs.php")
    fun getDutyLogs(@Body request: DutyLogRequest): Call<DutyLogResponse>

    @POST("student/submit_duty_log.php")
    fun submitTimeLog(@Body request: API.TimeLogRequest): Call<TimeLogResponse>

    @POST("student/submit_duty_log.php")
    fun submitLog(@Body request: API.TimeLogRequest): Call<TimeLogResponse>


    @POST("getPastLogs")
    fun getPastLogs(@Body request: PastLogsRequest): Call<PastLogsResponse>

    @GET("duty-logs") // Adjust this to match your API endpoint
    fun getDutyLogs(): Call<List<DutyLogItem>>

    @POST("insertTimeLog") // Adjust the endpoint accordingly
    fun insertTimeLog(@Body log: TimeLogRequest): Call<Void>



}
