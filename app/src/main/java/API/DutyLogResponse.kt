package API

data class DutyLogsResponse(
    val success: Boolean,
    val duty_logs: List<DutyLog>?
)

data class DutyLog(
    val id: Int,
    val duty_date: String,
    val time_in: String,
    val time_out: String?,
    val duration_hours: Double?,
    val status: String
)
