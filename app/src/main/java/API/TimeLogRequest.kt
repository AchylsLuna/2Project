package API

data class TimeLogRequest(
    val duty_date: String,
    val time_in: String,
    val time_out: String
)
