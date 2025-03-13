package API

import com.google.gson.annotations.SerializedName

data class TimeLogResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("logs") val logs: List<TimeLogEntry>? // Logs are inside this list
)

data class TimeLogEntry(
    @SerializedName("date") val date: String,
    @SerializedName("time_in") val timeIn: String,
    @SerializedName("time_out") val timeOut: String?,
    @SerializedName("status") val status: String // Make sure the API actually returns this field
)
