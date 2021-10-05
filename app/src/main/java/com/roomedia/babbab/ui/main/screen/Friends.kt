package com.roomedia.babbab.ui.main.screen

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.TextFieldValue
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

interface Friends {
    val currentUserUid: String
        get() = Firebase.auth.uid
            ?: throw IllegalAccessError("to enter Friends screen, user must sign in.")
    val currentUserNameOrEmail: String
        get() = Firebase.auth.currentUser?.let { it.displayName ?: it.email }
            ?: throw IllegalAccessError("to enter Friends screen, user must sign in.")

    fun MutableState<List<Pair<User, FriendshipState>>>.queryValue(queryText: String) {
        Firebase.database.getReference("user").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                value = if (queryText == "") {
                    (snapshot.getUserList("$currentUserUid/friends", FriendshipState.IS_FRIEND)
                            + snapshot.getUserList("$currentUserUid/sendRequest", FriendshipState.SEND_REQUEST)
                            + snapshot.getUserList("$currentUserUid/receiveRequest", FriendshipState.RECEIVE_REQUEST))
                } else {
                    val currentUser = snapshot.child(currentUserUid).getValue(User::class.java)
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
            in receiveRequest.values -> FriendshipState.RECEIVE_REQUEST
            else -> FriendshipState.IS_STRANGER
        }
    }

    private fun sendNotification(user: User, channel: NotificationChannelEnum, title: String, message: String) {
        user.devices.values.forEach { token ->
            val model = DeviceNotificationModel(
                to = token,
                senderId = currentUserUid,
                channelId = channel.id,
                title = title,
                body = message,
            )
            CoroutineScope(Dispatchers.IO).launch {
                val result = ApiClient.messageService.sendNotification(model)
                Timber.d("$result")
            }
        }
    }

    fun Context.sendRequestNotification(user: User, message: String) {
        sendNotification(
            user,
            NotificationChannelEnum.SendRequest,
            getString(R.string.request_friend_from, currentUserNameOrEmail),
            message
        )
    }

    fun Context.sendRequestAcceptedNotification(uid: String) {
        Firebase.database.getReference("user/$uid").get().addOnSuccessListener { snapshot ->
            snapshot.getValue(User::class.java)?.also { user ->
                sendNotification(
                    user,
                    NotificationChannelEnum.RequestAccepted,
                    getString(R.string.request_accepted_from, currentUserNameOrEmail),
                    "🥳❤️"
                )
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

    private fun removeDatabaseValue(userUid: String, otherUid: String) {
        Firebase.database.getReference("user").apply {
            listOf("friends", "sendRequest", "receiveRequest").forEach { dst ->
                child("$userUid/$dst").get().addOnSuccessListener { snapshot ->
                    snapshot.children.firstOrNull { it.value == otherUid }?.ref?.removeValue()
                }
                child("$otherUid/$dst").get().addOnSuccessListener { snapshot ->
                    snapshot.children.firstOrNull { it.value == userUid }?.ref?.removeValue()
                }
            }
        }
    }

    fun Context.getSyncDatabase(otherUid: String, friendshipState: FriendshipState, onSuccess: () -> Unit, onError: () -> Unit) {
        Firebase.database.getReference("user/$currentUserUid").get().addOnSuccessListener { snapshot ->
            val currentUser = snapshot.getValue(User::class.java)
                ?: throw IllegalAccessError("user must sign in.")

            if (currentUser.getFriendshipState(otherUid) == friendshipState) {
                onSuccess()
            } else {
                onError()
                Toast.makeText(this, "🤷😥🔄🙇", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun Context.sendRequest(receiver: User, message: String) {
        getSyncDatabase(
            otherUid = receiver.uid,
            friendshipState = FriendshipState.IS_STRANGER,
            onSuccess = {
                sendRequestNotification(receiver, message)
                setDatabaseValue(currentUserUid, receiver.uid, FriendshipEvent.ON_REQUEST)
            },
            onError = {},
        )
    }

    fun Context.cancelRequest(otherUid: String) {
        getSyncDatabase(
            otherUid = otherUid,
            friendshipState = FriendshipState.SEND_REQUEST,
            onSuccess = {
                removeDatabaseValue(currentUserUid, otherUid)
            },
            onError = {},
        )
    }

    fun Context.refuseRequest(otherUid: String) {
        getSyncDatabase(
            otherUid = otherUid,
            friendshipState = FriendshipState.RECEIVE_REQUEST,
            onSuccess = {
                removeDatabaseValue(currentUserUid, otherUid)
            },
            onError = {},
        )
    }

    fun Context.acceptRequest(otherUid: String, context: Context) {
        getSyncDatabase(
            otherUid = otherUid,
            friendshipState = FriendshipState.RECEIVE_REQUEST,
            onSuccess = {
                context.sendRequestAcceptedNotification(otherUid)
                removeDatabaseValue(currentUserUid, otherUid)
                setDatabaseValue(currentUserUid, otherUid, FriendshipEvent.ON_ACCEPT)
            },
            onError = {},
        )
    }

    fun Context.disconnectRequest(otherUid: String) {
        getSyncDatabase(
            otherUid = otherUid,
            friendshipState = FriendshipState.IS_FRIEND,
            onSuccess = {
                removeDatabaseValue(currentUserUid, otherUid)
            },
            onError = {},
        )
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
                        cancelRequest(receiver.uid)
                    },
                    refuseRequest = { receiver ->
                        refuseRequest(receiver.uid)
                    },
                    acceptRequest = { receiver ->
                        acceptRequest(receiver.uid, baseContext)
                    },
                    disconnectRequest = { receiver ->
                        disconnectRequest(receiver.uid)
                    },
                )
            }
        }
    }
}
