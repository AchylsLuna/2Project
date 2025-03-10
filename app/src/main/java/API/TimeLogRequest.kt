package API

import com.google.gson.annotations.SerializedName

data class TimeLogRequest(
    @SerializedName("duty_date") val duty_date: String,
    @SerializedName("time_in") val time_in: String,
    @SerializedName("time_out") val time_out: String
)
