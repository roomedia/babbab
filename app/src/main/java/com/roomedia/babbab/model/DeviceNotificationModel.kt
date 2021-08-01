package com.roomedia.babbab.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class DeviceNotificationModel(
    val to: String,
    val notification: NotificationModel
) : Parcelable

@Keep
@Parcelize
class NotificationModel(
    val title: String,
    val body: String,
    val image: String? = null
) : Parcelable