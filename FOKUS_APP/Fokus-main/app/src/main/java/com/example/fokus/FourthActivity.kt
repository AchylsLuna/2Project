package com.example.fokus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class FourthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fourth)

        val nxtBtn = findViewById<Button>(R.id.nxtBtn)

        nxtBtn.setOnClickListener{
            val Intent = Intent(this, SignupActivity::class.java)
            startActivity(Intent)
        }



        }
    }
