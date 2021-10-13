package com.roomedia.babbab.ui.main.text

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.roomedia.babbab.ui.main.button.SettingTextButton
import com.roomedia.babbab.ui.theme.BabbabTheme

@Composable
fun SectionText(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth()
            .padding(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = 4.dp,
            ),
        color = MaterialTheme.colors.onBackground.copy(0.7f),
    )
}

@Composable
fun SectionText(@StringRes textId: Int) {
    SectionText(text = stringResource(textId))
}

@Preview(
    name = "Light Theme",
    showBackground = true,
)
@Preview(
    name = "Dark Theme",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES,
)
@Composable
fun SectionTextPreview() {
    BabbabTheme {
        Scaffold {
            Column {
                SectionText("Section Text 1")
                SettingTextButton(text = "setting 1")
                SettingTextButton(text = "setting 2")
                Divider()

                SectionText("Section Text 2")
                SettingTextButton(text = "setting 3")
            }
        }
    }
}
