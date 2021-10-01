package com.roomedia.babbab.model

import androidx.annotation.Keep
import com.google.firebase.database.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class User(
    val displayName: String? = null,
    val email: String? = null,
    val friends: List<String> = listOf(),
    val pending: List<String> = listOf(),
)