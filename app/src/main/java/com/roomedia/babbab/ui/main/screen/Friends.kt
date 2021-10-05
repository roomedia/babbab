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
import com.roomedia.babbab.model.*
import com.roomedia.babbab.service.ApiClient
import com.roomedia.babbab.ui.main.userList.SearchBar
import com.roomedia.babbab.ui.main.userList.UserList
import com.roomedia.babbab.ui.theme.BabbabTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

interface Friends {
    val currentUserUid: String
        get() = Firebase.auth.uid
            ?: throw IllegalAccessError("to enter Friends screen, user must sign in.")

    fun MutableState<List<Pair<User, FriendshipState>>>.queryValue(queryText: String) {
        Firebase.database.getReference("user").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userSnapshot = snapshot.child(currentUserUid)
                value = if (queryText == "") {
                    (userSnapshot.getUserList("friends", FriendshipState.IS_FRIEND)
                            + userSnapshot.getUserList("sendRequest", FriendshipState.SEND_REQUEST)
                            + userSnapshot.getUserList("receiveRequest", FriendshipState.RECEIVE_REQUEST))
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
            in sendRequest.values -> FriendshipState.SEND_REQUEST
            else -> FriendshipState.IS_STRANGER
        }
    }

    fun AppCompatActivity.sendNotification(user: User, message: String) {
        val sender = Firebase.auth.currentUser?.let { it.displayName ?: it.email }
            ?: throw IllegalAccessError("to send request, user must sign in.")

        user.devices.values.forEach { token ->
            val model = DeviceNotificationModel(
                to = token,
                senderId = currentUserUid,
                channelId = NotificationChannelEnum.SendRequest.id,
                title = getString(R.string.request_friend_from, sender),
                body = message,
            )
            lifecycleScope.launch(Dispatchers.IO) {
                val result = ApiClient.messageService.sendNotification(model)
                Timber.d("$result")
            }
        }
    }

    private fun setDatabaseValue(userUid: String, otherUid: String, friendshipEvent: FriendshipEvent) {
        Firebase.database.getReference("user").apply {
            val key = push().key
            child("$userUid/${friendshipEvent.userDst}")
                .updateChildren(mapOf(key to otherUid))
            child("${otherUid}/${friendshipEvent.otherDst}")
                .updateChildren(mapOf(key to userUid))
        }
    }

    private fun removeDatabaseValue(userUid: String, otherUid: String, friendshipEvent: FriendshipEvent) {
        Firebase.database.getReference("user").apply {
            child("$userUid/${friendshipEvent.userDst}").get().addOnSuccessListener { snapshot ->
                snapshot.children.firstOrNull { it.value == otherUid }?.ref?.removeValue()
            }
            child("$otherUid/${friendshipEvent.otherDst}").get().addOnSuccessListener { snapshot ->
                snapshot.children.firstOrNull { it.value == userUid }?.ref?.removeValue()
            }
        }
    }

    fun AppCompatActivity.sendRequest(receiver: User, message: String) {
        sendNotification(receiver, message)
        setDatabaseValue(currentUserUid, receiver.uid, FriendshipEvent.ON_REQUEST)
    }

    fun refuseRequest(userUid: String, otherUid: String) {
        removeDatabaseValue(userUid, otherUid, FriendshipEvent.ON_REFUSE)
    }

    fun acceptRequest(userUid: String, otherUid: String) {
        removeDatabaseValue(userUid, otherUid, FriendshipEvent.ON_REFUSE)
        setDatabaseValue(userUid, otherUid, FriendshipEvent.ON_ACCEPT)
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
                    sendRequest = { receiver, message ->
                        sendRequest(receiver, message)
                    },
                    cancelRequest = { receiver ->
                        removeDatabaseValue(currentUserUid, receiver.uid, FriendshipEvent.ON_CANCEL)
                    },
                    disconnectRequest = { receiver ->
                        removeDatabaseValue(currentUserUid, receiver.uid, FriendshipEvent.ON_DISCONNECT)
                    },
                )
            }
        }
    }
}
