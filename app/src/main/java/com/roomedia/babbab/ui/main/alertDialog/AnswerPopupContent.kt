package com.roomedia.babbab.ui.main.alertDialog

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.roomedia.babbab.R
import com.roomedia.babbab.ui.main.button.ImageButton
import com.roomedia.babbab.ui.main.button.ThumbnailButton
import com.roomedia.babbab.ui.theme.BabbabTheme

@Composable
fun AnswerPopupContent(takePhoto: () -> Unit, selectPhoto: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth(),
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

        // TODO: Change to Recent Images
        listOf(
            R.drawable.ic_launcher_background,
            R.drawable.ic_launcher_foreground,
        ).forEachIndexed { idx, src ->
            Spacer(Modifier.width(8.dp))
            ThumbnailButton(
                painter = painterResource(src),
                contentDescription = stringResource(R.string.recent_photo, idx),
                onClick = {},
            )
        }
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
        AnswerPopupContent({}, {})
    }
}
