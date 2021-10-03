package com.roomedia.babbab.broadcastReceiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import timber.log.Timber

class FriendRequestBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        intent.extras?.getInt(KEY_NOTIFICATION_ID)?.let { notificationId ->
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .cancel(notificationId)
        }
        if (intent.getBooleanExtra(KEY_IS_ACCEPTED, false)) {
            Timber.d("accepted")
        } else {
            Timber.d("refused")
        }
    }

    companion object {
        private const val KEY_NOTIFICATION_ID = "notification_id"
        private const val KEY_IS_ACCEPTED = "is_accepted"

        fun onRefuse(context: Context) : PendingIntent {
            return Intent(context, FriendRequestBroadcastReceiver::class.java)
                .putExtra(KEY_IS_ACCEPTED, false)
                .toPendingIntent(context)
        }

        fun onAccept(context: Context) : PendingIntent {
            return Intent(context, FriendRequestBroadcastReceiver::class.java)
                .putExtra(KEY_IS_ACCEPTED, true)
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