package com.roomedia.babbab.ui.main.userList

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.roomedia.babbab.R
import com.roomedia.babbab.model.FriendshipEvent
import com.roomedia.babbab.model.FriendshipState
import com.roomedia.babbab.model.User
import com.roomedia.babbab.ui.main.button.BorderlessTextButton
import com.roomedia.babbab.ui.main.button.RoundedCornerTextButton
import com.roomedia.babbab.ui.theme.BabbabTheme
import com.roomedia.babbab.ui.theme.Shapes

@Composable
fun UserItem(
    user: User,
    friendshipState: MutableState<FriendshipState>,
    sendRequest: (User) -> Unit = {},
    cancelRequest: (User) -> Unit = {},
    disconnectRequest: (User) -> Unit = {},
) {
    val friendshipEvent = remember { mutableStateOf(FriendshipEvent.ON_CLEAR) }
    Card(
        modifier = Modifier
            .padding(start = 4.dp, top = 8.dp, end = 4.dp, bottom = 4.dp)
            .fillMaxWidth(),
        shape = Shapes.medium,
    ) {
        Row(Modifier.padding(12.dp)) {
            Image(
                // TODO: get current User Profile
                painter = rememberImagePainter(
                    data = "https://i.ibb.co/WgvgVzf/convert-icon-2.png",
                    builder = {
                        crossfade(true)
                    },
                ),
                contentDescription = stringResource(R.string.profile_image, user.displayName),
                modifier = Modifier
                    .size(40.dp)
                    .clip(Shapes.small),
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(user.displayName)
                Text(user.email)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterVertically),
                contentAlignment = Alignment.CenterEnd,
            ) {
                when (friendshipState.value) {
                    FriendshipState.IS_STRANGER -> RoundedCornerTextButton(text = "️add") {
                        friendshipEvent.value = FriendshipEvent.ON_REQUEST
                    }
                    FriendshipState.SEND_REQUEST -> RoundedCornerTextButton(text = "cancel") {
                        friendshipEvent.value = FriendshipEvent.ON_CANCEL
                    }
                    FriendshipState.RECEIVE_REQUEST -> {
                        Row {
                            RoundedCornerTextButton(text = "refuse") {
                                friendshipEvent.value = FriendshipEvent.ON_REFUSE
                            }
                            Spacer(Modifier.width(4.dp))
                            RoundedCornerTextButton(text = "accept") {
                                friendshipEvent.value = FriendshipEvent.ON_ACCEPT
                            }
                        }
                    }
                    FriendshipState.IS_FRIEND -> RoundedCornerTextButton(text = "️disconnect") {
                        friendshipEvent.value = FriendshipEvent.ON_DISCONNECT
                    }
                    FriendshipState.IS_ME -> RoundedCornerTextButton(text = "me") {}
                }
            }
        }
        when (friendshipEvent.value) {
            FriendshipEvent.ON_REQUEST -> {
                AlertDialog(
                    onDismissRequest = {
                        friendshipEvent.value = FriendshipEvent.ON_CLEAR
                    },
                    confirmButton = {
                        BorderlessTextButton(text = "✔️") {
                            sendRequest(user)
                            friendshipState.value = FriendshipState.SEND_REQUEST
                            friendshipEvent.value = FriendshipEvent.ON_CLEAR
                        }
                    },
                    dismissButton = {
                        BorderlessTextButton(text = "❌️") {
                            friendshipEvent.value = FriendshipEvent.ON_CLEAR
                        }
                    },
                    title = {
                        Text("👤+ ${user.displayName}")
                    },
                )
            }
            FriendshipEvent.ON_CANCEL -> {
                cancelRequest(user)
                friendshipState.value = FriendshipState.IS_STRANGER
            }
            FriendshipEvent.ON_DISCONNECT -> {
                AlertDialog(
                    onDismissRequest = {
                        friendshipEvent.value = FriendshipEvent.ON_CLEAR
                    },
                    confirmButton = {
                        BorderlessTextButton(text = "✔️") {
                            disconnectRequest(user)
                            friendshipState.value = FriendshipState.IS_STRANGER
                            friendshipEvent.value = FriendshipEvent.ON_CLEAR
                        }
                    },
                    dismissButton = {
                        BorderlessTextButton(text = "❌️") {
                            friendshipEvent.value = FriendshipEvent.ON_CLEAR
                        }
                    },
                    title = {
                        Text("👤x ${user.displayName}")
                    },
                )
            }
            else -> {}
        }
    }
}

@Preview(name = "Light Theme")
@Preview(
    name = "Dark Theme",
    uiMode = UI_MODE_NIGHT_YES,
)
@Composable
fun UserItemPreview() {
    BabbabTheme {
        Scaffold {
            Column {
                UserItem(
                    User("uid", "USER#1", "email@host.com"),
                    mutableStateOf(FriendshipState.IS_STRANGER)
                )
                UserItem(
                    User("uid", "USER#2", "email@host.com"),
                    mutableStateOf(FriendshipState.SEND_REQUEST)
                )
                UserItem(
                    User("uid", "USER#3", "email@host.com"),
                    mutableStateOf(FriendshipState.IS_FRIEND)
                )
                UserItem(
                    User("uid", "USER#4", "email@host.com"),
                    mutableStateOf(FriendshipState.IS_ME)
                )
                UserItem(
                    User("uid", "USER#5", "email@host.com"),
                    mutableStateOf(FriendshipState.RECEIVE_REQUEST)
                )
            }
        }
    }
}
