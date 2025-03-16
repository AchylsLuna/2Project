package com.example.scholarly

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SuccessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success)
    }
    val successContinue = findViewById<TextView>(R.id.successContinue)

    private fun setupListeners() {
        successContinue.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

    }
}