package com.roomedia.babbab.model

import androidx.annotation.Keep

@Keep
data class DeviceNotificationModel(
    val to: String,
    val notification: NotificationModel
)

@Keep
class NotificationModel(
    val title: String,
    val body: String,
    val image: String? = null
)