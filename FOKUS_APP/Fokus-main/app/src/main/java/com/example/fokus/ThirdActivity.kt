package com.example.fokus

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ThirdActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_third)

        val iNxtBtn = findViewById<ImageButton>(R.id.inxtBtn)

        iNxtBtn.setOnClickListener {
            val Intent = Intent(this, FourthActivity::class.java)
            startActivity(Intent)

        }

        val iBckBtn = findViewById<ImageButton>(R.id.ibckBtn)

        iBckBtn.setOnClickListener{
            val Intent = Intent(this, SecondActivity::class.java)
            startActivity(Intent)
        }

    }
}
