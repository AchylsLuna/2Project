package API

import com.google.gson.annotations.SerializedName

data class TotalHoursResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("total_hours") val total_hours: Double
)
