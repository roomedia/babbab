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
import com.roomedia.babbab.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        CoroutineScope(Dispatchers.IO).launch {
            Timber.d("From: ${remoteMessage.from}")
            val intent = Intent(baseContext, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val requestId = remoteMessage.senderId.hashCode()
            val pendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent =
                PendingIntent.getActivity(baseContext, requestId, intent, pendingIntentFlag)

            val title = remoteMessage.notification?.title ?: ""
            val body = remoteMessage.notification?.body ?: ""
            val image = remoteMessage.notification?.imageUrl?.let { imageUrl ->
                ImageRequest.Builder(baseContext)
                    .data(imageUrl)
                    .build()
                    .let { ImageLoader(baseContext).execute(it) }
                    .let { (it.drawable as BitmapDrawable).bitmap }
            }
            val clickActions = remoteMessage.notification?.clickAction?.let {
                Pair(
                    PendingIntent.getActivity(baseContext, requestId, intent, pendingIntentFlag),
                    PendingIntent.getActivity(baseContext, requestId, intent, pendingIntentFlag)
                )
            }

            val (channelId, channelName) = when {
                image != null -> Pair(
                    getString(R.string.answer_notification_channel_id),
                    getString(R.string.answer_notification_channel_name)
                )
                clickActions != null -> Pair(
                    getString(R.string.request_friend_notification_channel_id),
                    getString(R.string.request_friend_notification_channel_name)
                )
                else -> Pair(
                    getString(R.string.question_notification_channel_id),
                    getString(R.string.question_notification_channel_name)
                )
            }

            val notificationBuilder = NotificationCompat.Builder(baseContext, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .run {
                    if (image == null) return@run this
                    setLargeIcon(image)
                    setStyle(NotificationCompat.BigPictureStyle().bigPicture(image))
                }
                .run {
                    if (clickActions == null) return@run this
                    val (refuseRequest, acceptRequest) = clickActions
                    addAction(0, getString(R.string.refuse_friend_request), refuseRequest)
                    addAction(0, getString(R.string.accept_friend_request), acceptRequest)
                }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager.createNotificationChannel(channel)
            }
            notificationManager.notify(0, notificationBuilder.build())
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

    companion object {
        private val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    }
}