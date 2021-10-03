package com.roomedia.babbab.model

import androidx.annotation.Keep
import com.google.firebase.database.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val friends: Map<String, String> = mapOf(),
    val sendRequest: Map<String, String> = mapOf(),
    val receiveRequest: Map<String, String> = mapOf(),
    val devices: Map<String, String> = mapOf(),
)