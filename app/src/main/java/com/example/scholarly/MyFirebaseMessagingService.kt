package com.example.scholarly

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MyFirebaseMessagingService : FirebaseMessagingService() {
    data class NotificationItem(val title: String, val body: String)

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        remoteMessage.notification?.let {
            val title = it.title ?: "Default Title"
            val body = it.body ?: "Default Body"
            Log.d("FCM", "Message received: Title=$title, Body=$body")

            saveNotificationToList(title, body)
            sendNotification(title, body)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        // Note: No server sending; manually copy this token if it changes
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("FCM_TOKEN", token)
            apply()
        }
    }

    private fun saveNotificationToList(title: String, body: String) {
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val gson = Gson()

        val notificationsJson = sharedPreferences.getString("NOTIFICATIONS_LIST", "[]")
        val type = object : TypeToken<MutableList<NotificationItem>>() {}.type
        val notifications: MutableList<NotificationItem> = gson.fromJson(notificationsJson, type) ?: mutableListOf()

        notifications.add(0, NotificationItem(title, body))

        with(sharedPreferences.edit()) {
            putString("NOTIFICATIONS_LIST", gson.toJson(notifications))
            apply()
        }
    }

    private fun sendNotification(title: String, message: String) {
        val channelId = "default_channel_id"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Default Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, NotificationActivity::class.java).apply {
            putExtra("NOTIFICATION_TITLE", title)
            putExtra("NOTIFICATION_BODY", message)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}