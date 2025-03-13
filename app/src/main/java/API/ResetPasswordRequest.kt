package API

data class ResetPasswordRequest(
    val email: String,
    val student_id: String,
    val new_password: String
)
