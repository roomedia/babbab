package com.roomedia.babbab.ui.main.screen

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.TextFieldValue
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.roomedia.babbab.model.FriendshipState
import com.roomedia.babbab.model.User
import com.roomedia.babbab.ui.main.userList.SearchBar
import com.roomedia.babbab.ui.main.userList.UserList
import com.roomedia.babbab.ui.theme.BabbabTheme
import timber.log.Timber

interface Friends {
    fun AppCompatActivity.getUsers(queryText: String, userListState: MutableState<List<Pair<User, FriendshipState>>>) {
        Firebase.database.getReference("user").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (queryText == "") {
                    // TODO: get real friends
                    userListState.value = snapshot.children
                        .mapNotNull { it.getValue(User::class.java) }
                        .map { Pair(it, FriendshipState.IS_FRIEND) }
                } else {
                    // TODO: get friendship
                    userListState.value = snapshot.children
                        .mapNotNull { it.getValue(User::class.java) }
                        .filter { it.email == queryText || it.displayName == queryText }
                        .map { Pair(it, FriendshipState.IS_STRANGER) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e("${error.toException().stackTrace}")
            }
        })
    }

    @Composable
    fun AppCompatActivity.Friends() {
        val queryTextState = remember { mutableStateOf(TextFieldValue("")) }
        val userAndFriendshipListState = remember { mutableStateOf(listOf<Pair<User, FriendshipState>>()) }
        BabbabTheme {
            Scaffold(topBar = { SearchBar(queryTextState) }) {
                UserList(userAndFriendshipListState.value)
                getUsers(queryTextState.value.text, userAndFriendshipListState)
            }
        }
    }
}
