package com.roomedia.babbab.ui.main.alertDialog

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.roomedia.babbab.R
import com.roomedia.babbab.ui.main.button.ImageButton
import com.roomedia.babbab.ui.main.button.ThumbnailButton
import com.roomedia.babbab.ui.theme.BabbabTheme
import com.roomedia.babbab.ui.theme.Shapes

@Composable
fun AnswerPopupContent(
    takePhoto: () -> Unit,
    selectPhoto: () -> Unit,
    recentUris: List<Uri?>,
    targetUri: MutableState<Uri?>,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ImageButton(
                painter = painterResource(R.drawable.ic_take_photo),
                contentDescription = stringResource(R.string.take_photo),
                onClick = takePhoto,
            )
            Spacer(Modifier.width(8.dp))
            ImageButton(
                painter = painterResource(R.drawable.ic_select_photo),
                contentDescription = stringResource(R.string.select_photo),
                onClick = selectPhoto,
            )

            recentUris.forEachIndexed { idx, uri ->
                Spacer(Modifier.width(8.dp))
                if (uri == null) {
                    Spacer(Modifier.weight(1f))
                } else {
                    ThumbnailButton(
                        painter = rememberImagePainter(uri, builder = {
                            crossfade(true)
                        }),
                        contentDescription = stringResource(R.string.recent_photo, idx),
                        onClick = { targetUri.value = uri },
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        // TODO: Change to Guide Image
        val painter = if (targetUri.value == null) {
            painterResource(R.drawable.ic_launcher_background)
        } else {
            rememberImagePainter(targetUri.value, builder = {
                crossfade(true)
            })
        }
        Image(
            painter = painter,
            contentDescription = targetUri.value.toString(),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(Shapes.medium),
            contentScale = ContentScale.Crop,
        )
    }
}

@Preview(
    name = "Light Theme",
    showBackground = true,
)
@Preview(
    name = "Dark Theme",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun DefaultPreview() {
    BabbabTheme {
        AnswerPopupContent({}, {}, listOf(null, null), remember { mutableStateOf(null) })
    }
}
