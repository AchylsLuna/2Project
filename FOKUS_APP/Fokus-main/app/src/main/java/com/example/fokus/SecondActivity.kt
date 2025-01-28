package com.example.fokus

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class SecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_second)

        val iNxtBtn = findViewById<ImageButton>(R.id.inxtBtn)

        iNxtBtn.setOnClickListener {
            val Intent = Intent(this, ThirdActivity::class.java)
            startActivity(Intent)

        }
        val iBckBtn = findViewById<ImageButton>(R.id.ibckBtn)

        iBckBtn.setOnClickListener{
            val Intent = Intent(this, FirstActivity::class.java)
            startActivity(Intent)
        }
    }
}