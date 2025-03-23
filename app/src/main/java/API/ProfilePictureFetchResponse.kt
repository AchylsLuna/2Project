package API

data class ProfilePictureFetchResponse(
    val success: Boolean,
    val profilePicture: String? // Base64-encoded image or null
)