package com.example.scholarly

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
                selectedImageUri = uri
                profileImage.setImageURI(uri) // Display locally first
                uploadProfilePicture(uri) // Upload to server
            }
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
        val uploadButton = findViewById<ImageButton>(R.id.btnUploadProfilePic)
        val logoutButton = findViewById<Button>(R.id.btnLogout)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        loadProfileData(profileName, profileDetails)
        uploadButton.setOnClickListener { openImagePicker() }
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
                R.id.nav_profile -> true
                else -> false
            }
        }

        // Load existing profile picture if available
        loadProfilePicture()
    }

    private fun openImagePicker() {
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 100)
        } else {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openImagePicker()
        }
    }

    private fun uploadProfilePicture(uri: Uri) {
        val filePath = getRealPathFromURI(uri) ?: return
        val file = File(filePath)
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("profile_picture", file.name, requestBody)

        val sessionToken = sharedPreferences.getString("SESSION_TOKEN", "") ?: return
        val headers = mapOf("Authorization" to "Bearer $sessionToken")

        apiService.uploadProfilePicture(headers, imagePart).enqueue(object : Callback<ProfilePictureResponse> {
            override fun onResponse(call: Call<ProfilePictureResponse>, response: Response<ProfilePictureResponse>) {
                if (response.isSuccessful) {
                    val pictureUrl = response.body()?.profilePictureUrl
                    pictureUrl?.let {
                        sharedPreferences.edit().putString("PROFILE_PICTURE_URL", it).apply()
                        loadImageFromUrl(it) // Update UI with server URL
                    }
                } else {
                    // Revert to default if upload fails
                    profileImage.setImageResource(R.drawable.profile_con)
                }
            }

            override fun onFailure(call: Call<ProfilePictureResponse>, t: Throwable) {
                profileImage.setImageResource(R.drawable.profile_con)
            }
        })
    }

    private fun loadProfilePicture() {
        val pictureUrl = sharedPreferences.getString("PROFILE_PICTURE_URL", null)
        if (pictureUrl != null) {
            loadImageFromUrl(pictureUrl)
        } else {
            profileImage.setImageResource(R.drawable.profile_con)
        }
    }

    private fun loadImageFromUrl(url: String) {
        Glide.with(this).load(url).into(profileImage)
    }

    private fun getRealPathFromURI(uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            it.moveToFirst()
            val idx = it.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            it.getString(idx)
        }
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

// API Response Data Class
data class ProfilePictureResponse(
    val success: Boolean,
    val profilePictureUrl: String?
)