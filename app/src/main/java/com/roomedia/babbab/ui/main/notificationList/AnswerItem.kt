package com.roomedia.babbab.ui.main.notificationList

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import coil.compose.rememberImagePainter
import coil.size.OriginalSize
import com.roomedia.babbab.R
import com.roomedia.babbab.model.NotificationEvent
import com.roomedia.babbab.ui.theme.Shapes

@Composable
fun AnswerItem(answer: NotificationEvent.Answer) {
    Image(
        painter = rememberImagePainter(
            data = answer.imageUrl,
            builder = {
                size(OriginalSize)
                crossfade(true)
            }
        ),
        // TODO: get current User Name from uid
        contentDescription = stringResource(R.string.answer_from, answer.uid),
        modifier = Modifier
            .fillMaxWidth()
            .clip(Shapes.large),
        contentScale = ContentScale.FillWidth,
    )
}