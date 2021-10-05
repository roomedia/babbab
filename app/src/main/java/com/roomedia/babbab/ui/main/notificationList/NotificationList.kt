package com.roomedia.babbab.ui.main.notificationList

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.roomedia.babbab.model.NotificationEvent
import com.roomedia.babbab.ui.main.button.FullWeightTextButton
import com.roomedia.babbab.ui.theme.BabbabTheme

@Composable
fun NotificationList() {
    LazyColumn(Modifier.padding(bottom = 88.dp)) {
        // TODO: change to real data
        items(10) {
            if (it % 7 == 0) {
                NotificationItem(NotificationEvent.Question("USER#$it", "yy-MM-dd HH:mm"))
            } else {
                NotificationItem(NotificationEvent.Answer("User#$it", "yy-MM-dd HH:mm", "https://i.ibb.co/whTbKKD/Eok-Cph-XVQAk-PNw-T.jpg"))
            }
        }
    }
}

@Composable
fun SendNotificationButtons(showQuestionPopup: MutableState<Boolean>, showAnswerPopup: MutableState<Boolean>) {
    ConstraintLayout(Modifier.fillMaxHeight()) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .constrainAs(createRef()) {
                    bottom.linkTo(parent.bottom)
                },
        ) {
            FullWeightTextButton(text = "🤔🍚") { showQuestionPopup.value = true }
            Spacer(Modifier.width(4.dp))
            FullWeightTextButton(text = "🤤🍚") { showAnswerPopup.value = true }
        }
    }
}

@Preview(name = "Light Theme")
@Preview(
    name = "Dark Theme",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun NotificationListPreview() {
    val showQuestionPopup = remember { mutableStateOf(false) }
    val showAnswerPopup = remember { mutableStateOf(false) }

    BabbabTheme {
        Scaffold {
            NotificationList()
            SendNotificationButtons(showQuestionPopup, showAnswerPopup)
        }
    }
}
