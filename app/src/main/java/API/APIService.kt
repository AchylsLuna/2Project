package com.example.scholarly
import API.PastLogsResponse
import API.TimeLogRequest
import API.TimeLogResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Headers
import retrofit2.http.POST





interface APIService {
    @Headers("Content-Type: application/json")
    @POST("student/login.php")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>

    @POST("student/submit_duty_log.php")
    suspend fun submitTimeLog(
        @HeaderMap headers: Map<String, String>,
        @Body request: TimeLogRequest
    ): retrofit2.Response<TimeLogResponse>

    @GET("student/duty_logs.php") // Ensure this matches your backend endpoint
    fun getPastDutyLogs(@HeaderMap headers: Map<String, String>): Call<PastLogsResponse>
}

