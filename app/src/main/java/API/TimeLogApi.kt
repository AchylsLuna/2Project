import API.TimeLogRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface TimeLogApi {
    @POST("timelogs/insert") // Adjust this endpoint as per your API
    fun insertTimeLog(@Body log: TimeLogRequest): Call<Void> // Change response type if needed
}
