package com.roomedia.babbab.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.messaging
import com.roomedia.babbab.R
import com.roomedia.babbab.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Timber.d("From: ${remoteMessage.from}")
        val sender = remoteMessage.notification?.body ?: return
        val imageUrl = remoteMessage.notification?.imageUrl
        sendNotification(sender, imageUrl)
    }

    override fun onNewToken(token: String) {
        Timber.d("Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
        Timber.d("sendRegistrationTokenToServer($token)")
    }

    private fun sendNotification(sender: String, imageUrl: Uri? = null) = CoroutineScope(Dispatchers.IO).launch {
        val intent = Intent(baseContext, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(baseContext, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(baseContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(sender)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .run {
                if (imageUrl == null) {
                    setContentText(getString(R.string.question_text))
                } else {
                    val bitmapImage = ImageRequest.Builder(baseContext)
                        .data(imageUrl)
                        .build()
                        .let { ImageLoader(baseContext).execute(it) }
                        .let { (it.drawable as BitmapDrawable).bitmap }

                    setContentText(getString(R.string.answer_text))
                        .setLargeIcon(bitmapImage)
                        .setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmapImage))
                }
            }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                getString(R.string.default_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0, notificationBuilder.build())
    }
}