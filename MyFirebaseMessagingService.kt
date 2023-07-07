package com.zapp.app.fcm

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.zapp.app.R
import com.zapp.app.ui.home.view.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var fcmRepo: FCMRepo


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.w(TAG, "From: ${remoteMessage.from}")
        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            val data = remoteMessage.notification!!
            Log.w(TAG, "Message Notification Body: ${data.body}")
            sendNotification(data.body, data.title)
        } else if (remoteMessage.data.isNotEmpty()) { // Check if message contains a data payload.
            Log.w(TAG, "Message data payload: ${remoteMessage.data}")
            // Handle message within 10 seconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    sendNotification(remoteMessage.data)

                }
            } else sendNotification(remoteMessage.data)

        }


    }

    private fun sendNotification(body: Map<String, String>) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val requestCode = 0
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE,
        )

        val channelId = "fcm_default_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder =
            NotificationCompat.Builder(this, channelId).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(body["title"]).setContentText(body["body"]).setAutoCancel(true)
                .setSound(defaultSoundUri).setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Zapp Channel",
                NotificationManager.IMPORTANCE_HIGH,
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = System.currentTimeMillis()
        notificationManager.notify(notificationId.toInt(), notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        Log.w(TAG, "FCM TOken Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        @SuppressLint("HardwareIds")
        val androidId =
            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: "androidID"
        token?.let {
            fcmRepo.sendFcmTokenToServer(token, androidId)
        }
    }

    private fun sendNotification(body: String?, title: String?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val requestCode = 0
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE,
        )

        val channelId = "fcm_default_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder =
            NotificationCompat.Builder(this, channelId).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title).setContentText(body).setAutoCancel(true)
                .setSound(defaultSoundUri).setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Default Channel",
                NotificationManager.IMPORTANCE_HIGH,
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = System.currentTimeMillis()
        notificationManager.notify(notificationId.toInt(), notificationBuilder.build())
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

}