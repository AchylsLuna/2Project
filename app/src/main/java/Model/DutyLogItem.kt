package API

import com.google.gson.annotations.SerializedName

data class DutyLogItem(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("duty_date") val duty_date: String,
    @SerializedName("time_in") val time_in: String,
    @SerializedName("time_out") val time_out: String,
    @SerializedName("duration") val duration: String,
    @SerializedName("status") val status: String
)
