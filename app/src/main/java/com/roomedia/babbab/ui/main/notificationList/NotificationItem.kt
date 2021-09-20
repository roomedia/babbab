package com.roomedia.babbab.ui.main.notificationList

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.roomedia.babbab.model.Event
import com.roomedia.babbab.ui.theme.BabbabTheme
import com.roomedia.babbab.ui.theme.Shapes

@Composable
fun NotificationItem(event: Event) {
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
                        is Event.Question -> R.string.question_text
                        is Event.Answer -> R.string.answer_text
                    }
                    Text(stringResource(textId))
                }
            }
            Spacer(Modifier.height(12.dp))
            if (event is Event.Answer) {
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
fun DefaultPreview() {
    BabbabTheme {
        Scaffold {
            Column {
                NotificationItem(Event.Question("UserQuestion", "yy-MM-dd HH:mm"))
                NotificationItem(
                    Event.Answer(
                        "UserAnswer",
                        "yy-MM-dd HH:mm",
                        "https://i.ibb.co/whTbKKD/Eok-Cph-XVQAk-PNw-T.jpg"
                    )
                )
            }
        }
    }
}
