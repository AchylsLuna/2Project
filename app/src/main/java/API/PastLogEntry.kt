package API

data class PastLogEntry(
    val id: Int,
    val student_id: Int,
    val time_in: String,
    val time_out: String?,
    val total_hours: Float?,
    val duty_date: String?, // Added to match API response
    val status: String
)