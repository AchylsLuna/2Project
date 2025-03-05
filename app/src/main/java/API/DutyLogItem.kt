package API

data class DutyLogItem(
    val id: String,
    val duty_date: String,
    val time_in: String,
    val time_out: String,
    val duration: String,
    val status: String
)
