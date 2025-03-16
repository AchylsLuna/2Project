package com.example.scholarly

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class NotificationActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var recyclerView: RecyclerView
    private lateinit var notificationAdapter: NotificationAdapter
    private val notifications = mutableListOf<MyFirebaseMessagingService.NotificationItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notification)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        // Handle edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find views

        recyclerView = findViewById(R.id.notificationsRecyclerView)
        val clearNotificationButton = findViewById<TextView>(R.id.clearNotification)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Load notifications from SharedPreferences
        loadNotifications()

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        notificationAdapter = NotificationAdapter(notifications)
        recyclerView.adapter = notificationAdapter

        // Check if new notification data is available in the Intent
        val title = intent.getStringExtra("NOTIFICATION_TITLE")
        val body = intent.getStringExtra("NOTIFICATION_BODY")
        if (!title.isNullOrEmpty() && !body.isNullOrEmpty()) {
            // Add the new notification to the list and save it
            notifications.add(0, MyFirebaseMessagingService.NotificationItem(title, body))
            saveNotifications()
            notificationAdapter.notifyDataSetChanged()
        }

        // Set up Clear all button
        clearNotificationButton.setOnClickListener {
            clearNotifications()
        }


        // Bottom navigation setup
        bottomNavigation.selectedItemId = R.id.nav_notifications
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, LogsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_notifications -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadNotifications() {
        val gson = Gson()
        val notificationsJson = sharedPreferences.getString("NOTIFICATIONS_LIST", "[]")
        val type = object : TypeToken<MutableList<MyFirebaseMessagingService.NotificationItem>>() {}.type
        val savedNotifications: MutableList<MyFirebaseMessagingService.NotificationItem> = gson.fromJson(notificationsJson, type) ?: mutableListOf()
        notifications.clear()
        notifications.addAll(savedNotifications)
    }

    private fun saveNotifications() {
        val gson = Gson()
        with(sharedPreferences.edit()) {
            putString("NOTIFICATIONS_LIST", gson.toJson(notifications))
            apply()
        }
    }

    private fun clearNotifications() {
        notifications.clear()
        saveNotifications()
        notificationAdapter.notifyDataSetChanged()
    }
}