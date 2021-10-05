package com.roomedia.babbab.ui.main.notificationList

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.roomedia.babbab.R
import com.roomedia.babbab.model.NotificationEvent
import com.roomedia.babbab.ui.theme.BabbabTheme
import com.roomedia.babbab.ui.theme.Shapes

@Composable
fun NotificationItem(event: NotificationEvent) {
    Card(
        modifier = Modifier
            .padding(start = 4.dp, top = 8.dp, end = 4.dp, bottom = 4.dp)
            .fillMaxWidth(),
        shape = Shapes.medium,
    ) {
        Column(Modifier.padding(12.dp)) {
            Row {
                Image(
                    // TODO: get current User Profile
                    painter = rememberImagePainter(
                        data = "https://i.ibb.co/WgvgVzf/convert-icon-2.png",
                        builder = {
                            crossfade(true)
                        },
                    ),
                    // TODO: get current User Name from uid
                    contentDescription = stringResource(R.string.profile_image, event.uid),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(Shapes.small),
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Row {
                        // TODO: get current User Name from uid
                        Text(
                            text = event.uid,
                            modifier = Modifier.alignByBaseline(),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = event.time,
                            modifier = Modifier
                                .alignByBaseline()
                                .alpha(0.7f),
                            fontSize = 12.sp,
                        )
                    }
                    val textId = when (event) {
                        is NotificationEvent.Question -> R.string.question_text
                        is NotificationEvent.Answer -> R.string.answer_text
                    }
                    Text(stringResource(textId))
                }
            }
            if (event is NotificationEvent.Answer) {
                Spacer(Modifier.height(12.dp))
                AnswerItem(answer = event)
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
fun NotificationItemPreview() {
    BabbabTheme {
        Scaffold {
            Column {
                NotificationItem(NotificationEvent.Question("UserQuestion", "yy-MM-dd HH:mm"))
                NotificationItem(
                    NotificationEvent.Answer(
                        "UserAnswer",
                        "yy-MM-dd HH:mm",
                        "https://i.ibb.co/whTbKKD/Eok-Cph-XVQAk-PNw-T.jpg"
                    )
                )
            }
        }
    }
}
