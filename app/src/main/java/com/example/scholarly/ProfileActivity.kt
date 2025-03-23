package com.example.scholarly

import API.ProfilePictureFetchResponse
import API.ProfilePictureResponse
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

class ProfileActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var profileImage: ImageView
    private lateinit var apiService: APIService
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                Log.d("ProfileActivity", "Image selected: $uri")
                selectedImageUri = uri
                profileImage.setImageURI(uri)
                uploadProfilePicture(uri)
            } ?: run {
                Log.e("ProfileActivity", "No URI returned from gallery")
                Toast.makeText(this, "Failed to select image", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d("ProfileActivity", "Image picker canceled or failed: ${result.resultCode}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        apiService = ApiClient.retrofit.create(APIService::class.java)

        profileImage = findViewById(R.id.profileImage)
        val profileName = findViewById<TextView>(R.id.profileName)
        val profileDetails = findViewById<TextView>(R.id.profileDetails)
        val changeProfilePicButton = findViewById<Button>(R.id.btnUploadProfilePic)
        val logoutButton = findViewById<Button>(R.id.btnLogout)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        updateChangeProfilePicButtonText(changeProfilePicButton)

        loadProfileData(profileName, profileDetails)
        changeProfilePicButton.setOnClickListener {
            Log.d("ProfileActivity", "Change profile picture button clicked")
            openImagePicker()
        }
        logoutButton.setOnClickListener { performLogout() }

        bottomNavigation.selectedItemId = R.id.nav_profile
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, LogsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_notifications -> {
                    startActivity(Intent(this, NotificationActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_logs -> {
                    startActivity(Intent(this, PastLogsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }

        loadProfilePicture()
    }

    private fun updateChangeProfilePicButtonText(button: Button) {
        val sessionToken = sharedPreferences.getString("SESSION_TOKEN", "")
        if (sessionToken.isNullOrEmpty()) {
            button.text = "Upload Profile Picture"
            return
        }

        apiService.getProfilePicture(mapOf("Authorization" to "Bearer $sessionToken")).enqueue(object : Callback<ProfilePictureFetchResponse> {
            override fun onResponse(call: Call<ProfilePictureFetchResponse>, response: Response<ProfilePictureFetchResponse>) {
                if (response.isSuccessful && response.body()?.profilePicture != null) {
                    button.text = "Change Profile Picture"
                } else {
                    button.text = "Upload Profile Picture"
                }
            }

            override fun onFailure(call: Call<ProfilePictureFetchResponse>, t: Throwable) {
                button.text = "Upload Profile Picture"
            }
        })
    }

    private fun openImagePicker() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED) {
            Log.d("ProfileActivity", "Requesting permission: ${permissions[0]}")
            requestPermissions(permissions, 100)
        } else {
            Log.d("ProfileActivity", "Permission granted, launching image picker")
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("ProfileActivity", "Permission granted on request")
                openImagePicker()
            } else {
                Log.d("ProfileActivity", "Permission denied")
                Toast.makeText(this, "Permission denied. Cannot access gallery.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadProfilePicture(uri: Uri) {
        val file = getCompressedFileFromUri(uri) ?: run {
            Log.e("ProfileActivity", "Failed to compress and get file from URI: $uri")
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
            return
        }

        val mimeType = when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            else -> {
                Log.e("ProfileActivity", "Unsupported file extension: ${file.extension}")
                Toast.makeText(this, "Only JPG, PNG, and GIF are supported", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("profile_picture", file.name, requestBody)

        val sessionToken = sharedPreferences.getString("SESSION_TOKEN", "")
        if (sessionToken.isNullOrEmpty()) {
            Log.e("ProfileActivity", "Session token is missing")
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }

        val headers = mapOf("Authorization" to "Bearer $sessionToken")

        Log.d("ProfileActivity", "Uploading image: ${file.absolutePath} with MIME type: $mimeType, size: ${file.length()} bytes")
        apiService.uploadProfilePicture(headers, imagePart).enqueue(object : Callback<ProfilePictureResponse> {
            override fun onResponse(call: Call<ProfilePictureResponse>, response: Response<ProfilePictureResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("ProfileActivity", "Upload successful: ${body?.message}")
                    Toast.makeText(this@ProfileActivity, "Profile picture updated", Toast.LENGTH_SHORT).show()
                    loadProfilePicture()
                    updateChangeProfilePicButtonText(findViewById(R.id.btnUploadProfilePic))
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error body"
                    Log.e("ProfileActivity", "Upload failed: ${response.code()} - ${response.message()} - Raw response: $errorBody")
                    Toast.makeText(this@ProfileActivity, "Upload failed: $errorBody", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ProfilePictureResponse>, t: Throwable) {
                Log.e("ProfileActivity", "Upload error: ${t.message}")
                Toast.makeText(this@ProfileActivity, "Upload error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getCompressedFileFromUri(uri: Uri): File? {
        return try {
            val mimeType = contentResolver.getType(uri)
            val extension = when (mimeType) {
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                "image/gif" -> "gif"
                else -> "jpg"
            }

            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Target size: 2MB (well below default 4MB max_allowed_packet)
            val maxSizeBytes = 2 * 1024 * 1024
            var quality = 85
            var compressedBytes: ByteArray

            do {
                val outputStream = ByteArrayOutputStream()
                when (extension) {
                    "jpg", "jpeg" -> bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                    "png" -> bitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream)
                    "gif" -> bitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream)
                }
                compressedBytes = outputStream.toByteArray()
                Log.d("ProfileActivity", "Compressed image size at quality $quality: ${compressedBytes.size} bytes")

                if (compressedBytes.size > maxSizeBytes && quality > 10) {
                    quality -= 10 // Reduce quality incrementally
                } else {
                    break
                }
            } while (compressedBytes.size > maxSizeBytes)

            if (compressedBytes.size > maxSizeBytes) {
                Log.e("ProfileActivity", "Could not compress image below $maxSizeBytes bytes: ${compressedBytes.size} bytes")
                Toast.makeText(this, "Image too large even after maximum compression", Toast.LENGTH_SHORT).show()
                return null
            }

            val file = File(cacheDir, "temp_image_${System.currentTimeMillis()}.$extension")
            file.writeBytes(compressedBytes)
            Log.d("ProfileActivity", "File created: ${file.absolutePath} with MIME type: $mimeType")
            file
        } catch (e: Exception) {
            Log.e("ProfileActivity", "Error compressing file: ${e.message}")
            null
        }
    }

    private fun loadProfilePicture() {
        val sessionToken = sharedPreferences.getString("SESSION_TOKEN", "")
        if (sessionToken.isNullOrEmpty()) {
            Log.e("ProfileActivity", "Session token is missing for loading profile picture")
            profileImage.setImageResource(R.drawable.profile_con)
            return
        }

        val headers = mapOf("Authorization" to "Bearer $sessionToken")

        apiService.getProfilePicture(headers).enqueue(object : Callback<ProfilePictureFetchResponse> {
            override fun onResponse(call: Call<ProfilePictureFetchResponse>, response: Response<ProfilePictureFetchResponse>) {
                if (response.isSuccessful) {
                    val profilePictureData = response.body()?.profilePicture
                    if (profilePictureData != null) {
                        Log.d("ProfileActivity", "Profile picture fetched from database")
                        val imageBytes = Base64.decode(profilePictureData.split(",")[1], Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        profileImage.setImageBitmap(bitmap)
                    } else {
                        Log.d("ProfileActivity", "No profile picture in database")
                        profileImage.setImageResource(R.drawable.profile_con)
                    }
                } else {
                    Log.e("ProfileActivity", "Failed to fetch profile picture: ${response.code()} - ${response.message()}")
                    profileImage.setImageResource(R.drawable.profile_con)
                }
            }

            override fun onFailure(call: Call<ProfilePictureFetchResponse>, t: Throwable) {
                Log.e("ProfileActivity", "Error fetching profile picture: ${t.message}")
                profileImage.setImageResource(R.drawable.profile_con)
            }
        })
    }

    private fun loadProfileData(profileName: TextView, profileDetails: TextView) {
        val name = sharedPreferences.getString("STUDENT_NAME", "Unknown User")
        val scholarship = sharedPreferences.getString("SCHOLARSHIP_TYPE", "Not specified")
        val course = sharedPreferences.getString("COURSE", "Unknown course")
        val department = sharedPreferences.getString("DEPARTMENT", "Unknown department")
        val dutyStatus = sharedPreferences.getString("DUTY_STATUS", "No status")

        profileName.text = name
        profileDetails.text = """
            Scholarship Type: $scholarship
            Course: $course
            Department: $department
            Duty Status: $dutyStatus
        """.trimIndent()
    }

    private fun performLogout() {
        sharedPreferences.edit().clear().apply()
        Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(this)
        }
        finish()
    }
}