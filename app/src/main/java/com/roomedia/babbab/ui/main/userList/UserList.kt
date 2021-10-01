package com.roomedia.babbab.ui.main.userList

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.roomedia.babbab.model.FriendshipState
import com.roomedia.babbab.model.User

@Composable
fun UserList(userList: List<Pair<User, FriendshipState>>) {
    LazyColumn {
        items(userList.size) {
            val (user, state) = userList[it]
            UserItem(user, remember { mutableStateOf(state) })
        }
    }
}
