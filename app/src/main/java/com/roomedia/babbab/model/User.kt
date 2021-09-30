package com.roomedia.babbab.model

import androidx.annotation.Keep
import com.google.firebase.database.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class User(
    val uid: String,
    val displayName: String? = null,
    val email: String? = null,
)