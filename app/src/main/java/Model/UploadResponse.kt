package Model

import com.google.gson.annotations.SerializedName

data class UploadResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("profile_pic") val profilePic: String
)