package com.example.scholarly

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        val profileName = findViewById<TextView>(R.id.profileName)
        val profileDetails = findViewById<TextView>(R.id.profileDetails)
        val logoutButton = findViewById<Button>(R.id.btnLogout)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        loadProfileData(profileName, profileDetails)
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

    @Deprecated("Deprecated in favor of OnBackPressedDispatcher")
    override fun onBackPressed() {
        navigateToLogs()
        super.onBackPressed()
    }
}