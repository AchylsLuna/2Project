package API

import com.google.gson.annotations.SerializedName

data class TimeLogRequest(
    val student_id: String,  // Match API field name
    val date: String,
    val duty_date: String,// Ensure API expects this
    @SerializedName("time_in") val timeIn: String,
    @SerializedName("time_out") val timeOut: String
)
