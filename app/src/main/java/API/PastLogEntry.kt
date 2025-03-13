package API

import com.google.gson.annotations.SerializedName

data class PastLogEntry(
    @SerializedName("duty_date") val dutyDate: String,
    @SerializedName("time_in") val timeIn: String,
    @SerializedName("time_out") val timeOut: String?,
    @SerializedName("duration") val duration: String?,
    @SerializedName("status") val status: String
)
