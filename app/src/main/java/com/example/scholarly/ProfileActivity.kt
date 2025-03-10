package com.example.scholarly

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class ProfileActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        // Initialize views
        val profileName = findViewById<TextView>(R.id.profileName)
        val profileDetails = findViewById<TextView>(R.id.profileDetails)
        val logoutButton = findViewById<Button>(R.id.btnLogout)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        // Load profile data
        loadProfileData(profileName, profileDetails)

        // Set click listeners
        btnBack.setOnClickListener { navigateToLogs() }
        logoutButton.setOnClickListener { performLogout() }
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

    private fun navigateToLogs() {
        startActivity(Intent(this, LogsActivity::class.java))
        finish()
    }

    private fun performLogout() {
        sharedPreferences.edit().clear().apply()
        Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(this)
        }
        finish()
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        navigateToLogs()
        super.onBackPressed()
    }
}