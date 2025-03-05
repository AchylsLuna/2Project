package API

data class TimeLogItem(
    val id: String,
    val log_date: String,
    val time_in: String,
    val time_out: String,
    val status: String  // Keeping only status
)
