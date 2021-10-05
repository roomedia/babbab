package com.roomedia.babbab.ui.main.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.TextUnit

@Composable
fun BaseTextButton(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    text: String,
    fontSize: TextUnit = TextUnit.Unspecified,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        elevation = null,
        shape = shape,
        colors = colors,
        contentPadding = contentPadding,
    ) {
        Text(text, fontSize = fontSize)
    }
}
