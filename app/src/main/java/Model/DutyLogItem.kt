package API

import com.google.gson.annotations.SerializedName

data class DutyLogItem(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("duty_date") val dutyDate: String,
    @SerializedName("time_in") val timeIn: String,
    @SerializedName("time_out") val timeOut: String,
    @SerializedName("duration") val duration: String,
    @SerializedName("status") val status: String
)
