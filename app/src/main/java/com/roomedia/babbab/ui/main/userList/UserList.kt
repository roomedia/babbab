package com.roomedia.babbab.ui.main.userList

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.roomedia.babbab.model.FriendshipState
import com.roomedia.babbab.model.User
import com.roomedia.babbab.ui.theme.BabbabTheme

@Composable
fun UserList(queryTextState: MutableState<TextFieldValue>) {
    LazyColumn {
        if (queryTextState.value == TextFieldValue("")) {
            // TODO: change to friends list
            val states = (0..30).map { mutableStateOf(FriendshipState.IS_FRIEND) }
            items(states.size) {
                UserItem(
                    User("uid", "USER#$it", "email@host.com"),
                    states[it],
                )
            }
        } else {
            // TODO: change to queried user list
            val friendship = FriendshipState.values().map { mutableStateOf(it) }
            items(friendship.size) {
                UserItem(
                    User("uid", "USER#$it", "email@host.com"),
                    friendship[it],
                )
            }
        }
    }
}

@Preview(name = "Light Theme")
@Preview(
    name = "Dark Theme",
    uiMode = UI_MODE_NIGHT_YES,
)
@Composable
fun UserListPreview() {
    val textState = remember { mutableStateOf(TextFieldValue("")) }
    BabbabTheme {
        Scaffold(topBar = { SearchBar(textState) }) {
            UserList(textState)
        }
    }
}
