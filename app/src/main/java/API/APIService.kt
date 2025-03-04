import com.example.scholarly.LoginRequest
import com.example.scholarly.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface APIService {
    @Headers("Content-Type: application/json")
    @POST("login.php")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>
}
