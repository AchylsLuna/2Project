package com.example.scholarly
import API.PastLogsResponse
import API.ProfilePictureFetchResponse
import API.ProfilePictureResponse
import API.ResetPasswordRequest
import API.ResetPasswordResponse
import API.TimeLogRequest
import API.TimeLogResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


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

    @POST("student/reset_password.php")
    fun resetPassword(@Body request: ResetPasswordRequest): Call<ResetPasswordResponse>

    @Multipart
    @POST("student/upload_profile_picture.php")
    fun uploadProfilePicture(
        @HeaderMap headers: Map<String, String>,
        @Part image: MultipartBody.Part
    ): Call<ProfilePictureResponse>

    @GET("student/get_profile_picture.php")
    fun getProfilePicture(
        @HeaderMap headers: Map<String, String>
    ): Call<ProfilePictureFetchResponse>

}

