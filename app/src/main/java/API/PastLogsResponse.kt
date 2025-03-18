package API

data class PastLogsResponse(
    val success: Boolean,
    val logs: List<PastLogEntry>
)