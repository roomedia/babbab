package com.roomedia.babbab.model

enum class FriendshipEvent(val userDst: String = "", val otherDst: String = "") {
    ON_CLEAR,
    ON_REQUEST("sendRequest", "receiveRequest"),
    ON_CANCEL("sendRequest", "receiveRequest"),
    ON_REFUSE("receiveRequest", "sendRequest"),
    ON_ACCEPT("friends", "friends"),
    ON_DISCONNECT("friends", "friends"),
}