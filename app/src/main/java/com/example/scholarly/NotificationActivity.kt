package com.example.scholarly

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ImageButton
import android.widget.TextView

class NotificationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notification)

        // Apply window insets for edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find views
        val backButton = findViewById<ImageButton>(R.id.backButton)
        val titleTextView = findViewById<TextView>(R.id.notificationTitle)
        val bodyTextView = findViewById<TextView>(R.id.notificationBody)

        // Get notification data from Intent
        val title = intent.getStringExtra("NOTIFICATION_TITLE") ?: "No Title"
        val body = intent.getStringExtra("NOTIFICATION_BODY") ?: "No Body"

        // Display notification data
        titleTextView.text = title
        bodyTextView.text = body

        // Back button functionality
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close NotificationActivity after navigating to MainActivity
        }
    }
}