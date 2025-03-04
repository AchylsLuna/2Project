package com.example.scholarly

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loginButton = findViewById<Button>(R.id.signInbtn)
        loginButton.setOnClickListener {
            // Navigate to LogsActivity
            val intent = Intent(this, LogsActivity::class.java)
            startActivity(intent)
        }
    }
}
