package API

data class DutyLogResponse(
    val success: Boolean,
    val message: String,
    val logs: List<DutyLogItem>
)
