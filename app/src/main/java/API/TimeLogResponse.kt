package API

import com.google.gson.annotations.SerializedName

data class TimeLogResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)
