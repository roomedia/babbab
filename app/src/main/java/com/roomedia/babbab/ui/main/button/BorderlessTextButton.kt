package com.roomedia.babbab.ui.main.button

import android.content.res.Configuration
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.roomedia.babbab.ui.theme.BabbabTheme

@Composable
fun BorderlessTextButton(text: String, onClick: () -> Unit) {
    BaseTextButton(
        text = text,
        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.background),
        onClick = onClick,
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
fun DefaultPreview() {
    BabbabTheme {
        BorderlessTextButton("text") {}
    }
}
