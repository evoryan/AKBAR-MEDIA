package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.ui.data.UserSession
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val sharedPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val activeDbName = UserSession.currentUser.value?.db_name
            ?: sharedPrefs.getString("user_db_name", null)

        // 1. Jika tidak ada sesi user/tenant yang sedang login di perangkat ini, abaikan notifikasi
        if (activeDbName.isNullOrBlank()) {
            Log.d("FCM", "Notifikasi diabaikan: Tidak ada sesi tenant aktif pada perangkat ini.")
            return
        }

        // 2. Isolasi per database: Cek apakah payload db_name cocok dengan database tenant yang sedang aktif
        val incomingDbName = remoteMessage.data["db_name"]
        if (!incomingDbName.isNullOrBlank() && !incomingDbName.equals(activeDbName, ignoreCase = true)) {
            Log.w("FCM", "Notifikasi diabaikan: Berasal dari database tenant berbeda ($incomingDbName != $activeDbName)")
            // Otomatis bersihkan (unsubscribe) dari topik database asing ini jika pernah tersimpan
            val staleTopic = UserSession.getTenantTopic(incomingDbName)
            if (staleTopic != null) {
                try {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(staleTopic)
                } catch (e: Throwable) {
                    // Ignore
                }
            }
            return
        }

        // 3. Verifikasi asal topik (jika diterima melalui FCM topic)
        val fromTopic = remoteMessage.from
        val expectedTopic = UserSession.getTenantTopic(activeDbName)
        if (fromTopic != null && fromTopic.startsWith("/topics/tenant_")) {
            val topicReceived = fromTopic.removePrefix("/topics/")
            if (topicReceived == "tenant_superadmin") {
                // Topic global superadmin sudah usang, segera unsubscribe
                try {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("tenant_superadmin")
                } catch (e: Throwable) {}
                return
            }
            if (expectedTopic != null && topicReceived != expectedTopic) {
                Log.w("FCM", "Notifikasi diabaikan: Topik tidak sesuai ($topicReceived vs aktif: $expectedTopic)")
                try {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(topicReceived)
                } catch (e: Throwable) {}
                return
            }
        }

        val title = remoteMessage.notification?.title 
            ?: remoteMessage.data["title"] 
            ?: "Pembayaran Diterima"
        val body = remoteMessage.notification?.body 
            ?: remoteMessage.data["body"] 
            ?: ""

        if (title.isNotBlank() || body.isNotBlank()) {
            sendNotification(title, body, remoteMessage.data)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Token baru FCM didapatkan: $token")
        // Pastikan topik tenant aktif disubscribe ulang jika ada session
        val sharedPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val activeDbName = UserSession.currentUser.value?.db_name
            ?: sharedPrefs.getString("user_db_name", null)
        val topic = UserSession.getTenantTopic(activeDbName)
        if (topic != null) {
            UserSession.safeSubscribeToTopic(this, topic)
        }
    }

    private fun sendNotification(title: String, body: String, data: Map<String, String>) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("notification_type", data["type"] ?: "payment")
            putExtra("db_name", data["db_name"] ?: "")
            putExtra("customer_name", data["customer_name"] ?: "")
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 
            Random.nextInt(), 
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = "billing_notifications"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notifikasi Pembayaran",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Pemberitahuan pembayaran tagihan pelanggan per tenant database"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(Random.nextInt(), notificationBuilder.build())
    }
}
