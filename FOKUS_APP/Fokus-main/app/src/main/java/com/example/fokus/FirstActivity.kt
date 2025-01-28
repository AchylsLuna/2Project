package com.example.fokus
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class FirstActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_first)

        val iNxtBtn = findViewById<ImageButton>(R.id.inxtBtn)

        iNxtBtn.setOnClickListener{
            val Intent = Intent(this, SecondActivity::class.java)
            startActivity(Intent)
        }

        }

    }










