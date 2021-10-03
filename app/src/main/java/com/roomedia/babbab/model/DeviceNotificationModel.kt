package com.roomedia.babbab.model

import androidx.annotation.Keep

@Keep
data class DeviceNotificationModel(
    val to: String,
    val notification: NotificationModel,
) {
    constructor(
        to: String,
        channelId: String,
        title: String,
        body: String,
        image: String? = null,
        senderId: String? = null,
    ) : this(to, NotificationModel(channelId, title, body, image, senderId))
}

@Keep
data class NotificationModel(
    val android_channel_id: String,
    val title: String,
    val body: String,
    val image: String? = null,
    val tag: String? = null,
)