package com.roomedia.babbab.ui.main.screen

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.roomedia.babbab.R
import com.roomedia.babbab.model.DeviceNotificationModel
import com.roomedia.babbab.model.FriendshipState
import com.roomedia.babbab.model.NotificationChannelEnum
import com.roomedia.babbab.model.User
import com.roomedia.babbab.service.ApiClient
import com.roomedia.babbab.ui.main.userList.SearchBar
import com.roomedia.babbab.ui.main.userList.UserList
import com.roomedia.babbab.ui.theme.BabbabTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

interface Friends {
    fun MutableState<List<Pair<User, FriendshipState>>>.queryValue(queryText: String) {
        Firebase.database.getReference("user").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentUserUid = Firebase.auth.currentUser?.uid
                    ?: throw IllegalAccessError("to send request, user must sign in.")
                val userSnapshot = snapshot.child(currentUserUid)

                value = if (queryText == "") {
                    (userSnapshot.getUserList("friends", FriendshipState.IS_FRIEND)
                            + userSnapshot.getUserList("sendRequest", FriendshipState.PENDING_RESPONSE)
                            + userSnapshot.getUserList("receiveRequest", FriendshipState.IS_STRANGER))
                } else {
                    val currentUser = userSnapshot.getValue(User::class.java)
                        ?: throw IllegalAccessError("to send request, user must sign in.")

                    snapshot.queryUsers(queryText)
                        .map { Pair(it, currentUser.getFriendshipState(it.uid)) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e("${error.toException().stackTrace}")
            }
        })
    }

    fun DataSnapshot.getUserList(category: String, friendshipState: FriendshipState): List<Pair<User, FriendshipState>> {
        return child(category).children
            .mapNotNull { child("${it.value}").getValue(User::class.java) }
            .map { Pair(it, friendshipState) }
    }

    fun DataSnapshot.queryUsers(queryText: String): List<User> {
        return children.mapNotNull { it.getValue(User::class.java) }
            .filter { it.email == queryText || it.displayName == queryText }
    }

    fun User.getFriendshipState(uid: String): FriendshipState {
        return when (uid) {
            this.uid -> FriendshipState.IS_ME
            in friends.values -> FriendshipState.IS_FRIEND
            in sendRequest.values -> FriendshipState.PENDING_RESPONSE
            else -> FriendshipState.IS_STRANGER
        }
    }

    fun AppCompatActivity.sendNotification(user: User) {
        val sender = Firebase.auth.currentUser?.let { it.displayName ?: it.email }
            ?: throw IllegalAccessError("to send request, user must sign in.")

        user.devices.values.forEach { token ->
            val model = DeviceNotificationModel(
                to = token,
                channelId = NotificationChannelEnum.SendRequest.id,
                title = getString(R.string.request_friend_from, sender),
                body = getString(R.string.request_friend_text),
            )
            lifecycleScope.launch(Dispatchers.IO) {
                val result = ApiClient.messageService.sendNotification(model)
                Timber.d("$result")
            }
        }
    }

    fun sendRequestToDatabase(user: User) {
        val uid = Firebase.auth.currentUser?.uid
            ?: throw IllegalAccessError("to send request, requester must have uid.")

        Firebase.database.getReference("user").apply {
            val sendRequestRef = child("$uid/sendRequest")
            val updatedSendRequest = mapOf(
                sendRequestRef.push().key to user.uid,
            )
            sendRequestRef.updateChildren(updatedSendRequest)
                .addOnFailureListener { Timber.e(it.stackTraceToString()) }

            val receiveRequestRef = child("${user.uid}/receiveRequest")
            val updatedReceiveRequest = mapOf(
                receiveRequestRef.push().key to uid,
            )
            receiveRequestRef.updateChildren(updatedReceiveRequest)
                .addOnFailureListener { Timber.e(it.stackTraceToString()) }
        }
    }

    @Composable
    fun AppCompatActivity.Friends() {
        val queryTextState = remember { mutableStateOf(TextFieldValue("")) }
        val userAndFriendshipListState = remember { mutableStateOf(listOf<Pair<User, FriendshipState>>()) }
        userAndFriendshipListState.queryValue(queryTextState.value.text)

        BabbabTheme {
            Scaffold(topBar = { SearchBar(queryTextState) }) {
                UserList(
                    userList = userAndFriendshipListState.value,
                    sendRequest = { user ->
                        sendNotification(user)
                        sendRequestToDatabase(user)
                    },
                    cancelRequest = {},
                    disconnectRequest = {},
                )
            }
        }
    }
}
