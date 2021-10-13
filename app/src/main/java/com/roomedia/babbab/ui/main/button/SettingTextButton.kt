package com.roomedia.babbab.ui.main.button

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roomedia.babbab.R
import com.roomedia.babbab.ui.theme.BabbabTheme

@Composable
fun SettingTextButton(
    modifier: Modifier = Modifier,
    text: String,
    subtext: String? = null,
    onClick: (() -> Unit)? = null,
    showArrow: Boolean = true,
) {
    Button(
        onClick = onClick ?: {},
        modifier = modifier.fillMaxWidth(),
        enabled = onClick != null,
        elevation = null,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.background,
            disabledBackgroundColor = MaterialTheme.colors.background,
            disabledContentColor = contentColorFor(MaterialTheme.colors.background),
        ),
    ) {
        Text(text, modifier = Modifier.alignByBaseline())
        if (subtext != null) {
            Spacer(Modifier.width(4.dp))
            Text(
                text = subtext,
                modifier = Modifier.alignByBaseline(),
                color = MaterialTheme.colors.onBackground.copy(0.6f),
                fontSize = 12.sp,
            )
        }
        Box(Modifier.fillMaxWidth().alignByBaseline(), Alignment.CenterEnd) {
            if (showArrow) {
                Icon(Icons.Default.KeyboardArrowRight, stringResource(R.string.see_more))
            }
        }
    }
}

@Composable
fun SettingTextButton(
    modifier: Modifier = Modifier,
    @StringRes textId: Int,
    @StringRes subtextId: Int? = null,
    onClick: (() -> Unit)? = null,
    showArrow: Boolean = true,
) {
    SettingTextButton(
        modifier = modifier,
        text = stringResource(textId),
        subtext = subtextId?.let { stringResource(it) },
        onClick = onClick,
        showArrow = showArrow,
    )
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
fun SettingTextButtonPreview() {
    BabbabTheme {
        Column {
            SettingTextButton(
                text = "TEXT",
                showArrow = false,
            )
            SettingTextButton(
                textId = R.string.app_name,
                showArrow = false,
            )
            SettingTextButton(
                text = "TEXT",
                subtext = "subtext",
                onClick = {},
            )
            SettingTextButton(
                textId = R.string.app_name,
                subtextId = R.string.app_name,
                onClick = {},
            )
        }
    }
}
