package API

data class TimeLogRequest(
    val student_id: String,
    val duty_date: String,
    val time_in: String,
    val time_out: String,
)
