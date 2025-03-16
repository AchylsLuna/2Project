package com.example.scholarly // Adjust this to match your actual package

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // Handle incoming FCM messages here
        remoteMessage.notification?.let {
            val title = it.title ?: "Default Title"
            val body = it.body ?: "Default Body"
            Log.d("FCM", "Message received: Title=$title, Body=$body")
            sendNotification(title, body) // Show notification
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Handle token refresh here
        Log.d("FCM", "New token: $token")
        // Example: Send the token to your server if needed
        // You could call a function here to upload the token to your backend
    }

    private fun sendNotification(title: String, message: String) {
        val channelId = "default_channel_id"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android 8.0+ (API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Default Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Must match your manifest's meta-data
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true) // Dismisses when tapped
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Show the notification with a unique ID
        notificationManager.notify(1, notificationBuilder.build())
    }
}