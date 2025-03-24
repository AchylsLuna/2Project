package API

data class PastLogEntry(
    val time_in: String,
    val time_out: String?,
    val total_hours: Double?,
    val duty_date: String,
    val status: String
)