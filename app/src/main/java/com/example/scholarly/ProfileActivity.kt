package com.example.scholarly

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)  // Match MainActivity's prefs

        // Initialize views
        val profileName = findViewById<TextView>(R.id.profileName)
        val profileDetails = findViewById<TextView>(R.id.profileDetails)
        val logoutButton = findViewById<Button>(R.id.btnLogout)

        // Get student data from SharedPreferences
        val name = sharedPreferences.getString("STUDENT_NAME", "")
        val scholarshipType = sharedPreferences.getString("SCHOLARSHIP_TYPE", "N/A")
        val course = sharedPreferences.getString("COURSE", "")
        val department = sharedPreferences.getString("DEPARTMENT", "")
        val dutyStatus = sharedPreferences.getString("DUTY_STATUS", "")

        // Set profile information
        profileName.text = name ?: "No Name Found"

        val detailsText = """
            Scholarship Type: $scholarshipType
            Course: $course
            Department: $department
            Duty Status: $dutyStatus
        """.trimIndent()

        profileDetails.text = detailsText

        logoutButton.setOnClickListener {
            // Clear all preferences
            sharedPreferences.edit().clear().apply()

            // Return to login
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}