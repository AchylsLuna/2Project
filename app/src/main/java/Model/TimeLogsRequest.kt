data class TimeLogRequest(
    val student_id: String,
    val date: String,
    val time_in: String? = null,
    val time_out: String? = null
)
