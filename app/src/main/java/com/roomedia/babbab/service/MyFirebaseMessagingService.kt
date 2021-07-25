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
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.roomedia.babbab.R
import com.roomedia.babbab.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Timber.tag(TAG).d("From: ${remoteMessage.from}")
        remoteMessage.notification?.apply {
            val sender = body ?: ""
            val imageUrl = imageUrl ?: Uri.EMPTY
            sendNotification(sender, imageUrl)
        }
    }

    override fun onNewToken(token: String) {
        Timber.tag(TAG).d("Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
        Timber.tag(TAG).d("sendRegistrationTokenToServer($token)")
    }

    private fun sendNotification(sender: String, imageUrl: Uri) = CoroutineScope(Dispatchers.IO).launch {
        val intent = Intent(baseContext, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(baseContext, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val bitmapImage = ImageRequest.Builder(baseContext)
            .data(imageUrl)
            .build()
            .let { ImageLoader(baseContext).execute(it) }
            .let { (it.drawable as BitmapDrawable).bitmap }
        val notificationBuilder = NotificationCompat.Builder(baseContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("$sender 🙋💬🍚🤤")
            .setLargeIcon(bitmapImage)
            .setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmapImage))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                getString(R.string.default_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}