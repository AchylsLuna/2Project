package API


data class PastLogEntry(
    val id: Int,
    val duty_date: String,
    val time_in: String,
    val time_out: String?,
    val status: String,
    val total_hours: Double?,

)