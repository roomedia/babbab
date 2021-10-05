package com.roomedia.babbab.model

import androidx.annotation.Keep

@Keep
sealed class NotificationEvent private constructor(
    open val uid: String,
    open val time: String,
) {
    data class Question(
        override val uid: String,
        override val time: String,
    ) : NotificationEvent(uid, time)

    data class Answer(
        override val uid: String,
        override val time: String,
        val imageUrl: String,
    ) : NotificationEvent(uid, time)
}
