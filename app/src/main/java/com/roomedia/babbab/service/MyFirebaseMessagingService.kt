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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.roomedia.babbab.R
import com.roomedia.babbab.broadcastReceiver.FriendRequestBroadcastReceiver
import com.roomedia.babbab.model.NotificationChannelEnum
import com.roomedia.babbab.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        CoroutineScope(Dispatchers.IO).launch {
            Timber.d("From: ${remoteMessage.from}")
            val requestId = remoteMessage.senderId.hashCode()
            val title = remoteMessage.notification?.title ?: ""
            val body = remoteMessage.notification?.body ?: ""
            val image = remoteMessage.notification?.imageUrl?.let { imageUrl ->
                ImageRequest.Builder(baseContext)
                    .data(imageUrl)
                    .build()
                    .let { ImageLoader(baseContext).execute(it) }
                    .let { (it.drawable as BitmapDrawable).bitmap }
            }

            val (channelId, channelName) = remoteMessage.notification?.channelId?.let { channelId ->
                NotificationChannelEnum.values()
                    .first { channelId == it.id }
                    .run { Pair(id, channelName) }
            } ?: throw IllegalArgumentException("remote message must contain channel id")

            val notificationBuilder = NotificationCompat.Builder(baseContext, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setSound(DEFAULT_SOUND_URI)
                .setContentIntent(getContentIntent(requestId))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .run {
                    if (image == null) return@run this
                    setLargeIcon(image)
                    setStyle(NotificationCompat.BigPictureStyle().bigPicture(image))
                }
                .run {
                    if (channelId != NotificationChannelEnum.SendRequest.id) return@run this
                    val senderId = remoteMessage.notification?.tag
                        ?: throw java.lang.IllegalArgumentException("send request message must contain sender id as tag")
                    val refuseRequest = FriendRequestBroadcastReceiver.onRefuse(baseContext, senderId)
                    val acceptRequest = FriendRequestBroadcastReceiver.onAccept(baseContext, senderId)
                    addAction(0, "❌", refuseRequest)
                    addAction(0, "✔️", acceptRequest)
                }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }
            notificationManager.notify(baseContext.hashCode(), notificationBuilder.build())
        }
    }

    override fun onNewToken(token: String) {
        Timber.d("Refreshed token: $token")
        Firebase.auth.currentUser?.run {
            Firebase.database.getReference("user/$uid/devices").apply {
                updateChildren(mapOf(push().key to token))
            }
        }
    }

    private fun getContentIntent(requestId: Int) : PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getActivity(this, requestId, intent, pendingIntentFlag)
    }

    companion object {
        private val DEFAULT_SOUND_URI: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    }
}