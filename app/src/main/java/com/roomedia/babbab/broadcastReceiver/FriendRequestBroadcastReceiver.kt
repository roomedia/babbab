package com.roomedia.babbab.broadcastReceiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.roomedia.babbab.ui.main.screen.Friends

class FriendRequestBroadcastReceiver : BroadcastReceiver(), Friends {
    override fun onReceive(context: Context, intent: Intent) {
        intent.extras?.getInt(KEY_NOTIFICATION_ID)?.let { notificationId ->
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .cancel(notificationId)
        }
        val senderId = intent.getStringExtra(KEY_SENDER_ID) ?: return
        if (intent.getBooleanExtra(KEY_IS_ACCEPTED, false)) {
            acceptRequest(currentUserUid, senderId, context)
        } else {
            refuseRequest(currentUserUid, senderId)
        }
    }

    companion object {
        private const val KEY_NOTIFICATION_ID = "notification_id"
        private const val KEY_IS_ACCEPTED = "is_accepted"
        private const val KEY_SENDER_ID = "sender_id"

        fun onRefuse(context: Context, senderId: String) : PendingIntent {
            return Intent(context, FriendRequestBroadcastReceiver::class.java)
                .putExtra(KEY_IS_ACCEPTED, false)
                .putExtra(KEY_SENDER_ID, senderId)
                .toPendingIntent(context)
        }

        fun onAccept(context: Context, senderId: String) : PendingIntent {
            return Intent(context, FriendRequestBroadcastReceiver::class.java)
                .putExtra(KEY_IS_ACCEPTED, true)
                .putExtra(KEY_SENDER_ID, senderId)
                .toPendingIntent(context)
        }

        private fun Intent.toPendingIntent(context: Context) : PendingIntent {
            val pendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            return PendingIntent.getBroadcast(context, hashCode(), this, pendingIntentFlag)
        }
    }
}