package API

data class PastLogsResponse(
    val success: Boolean,
    val logs: List<PastLogEntry>
)

data class PastLogEntry(
    val date: String,
    val timeIn: String,
    val timeOut: String?,  // Nullable to handle missing data
    val duration: String?,
    val status: String
)
