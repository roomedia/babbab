package com.roomedia.babbab.ui.main.button

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.roomedia.babbab.ui.theme.BabbabTheme

@Composable
fun RoundedCornerTextButton(text: String, onClick: () -> Unit) {
    BaseTextButton(
        modifier = Modifier.height(24.dp),
        shape = RoundedCornerShape(50),
        contentPadding = PaddingValues(0.dp),
        text = text,
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
    uiMode = UI_MODE_NIGHT_YES,
)
@Composable
fun BorderTextButtonPreview() {
    BabbabTheme {
        RoundedCornerTextButton("text") {}
    }
}
