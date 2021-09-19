package com.roomedia.babbab.ui.main.button

import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit

@Composable
fun BaseTextButton(
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    text: String,
    fontSize: TextUnit = TextUnit.Unspecified,
    onClick: () -> Unit,
) {
    Button(onClick = onClick, modifier = modifier, elevation = null, colors = colors) {
        Text(text, fontSize = fontSize)
    }
}
