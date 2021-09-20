package com.roomedia.babbab.ui.main.button

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

@Composable
fun RowScope.ImageButton(painter: Painter, contentDescription: String, onClick: () -> Unit) {
    BaseRowImageButton(
        contentPadding = 16.dp,
        painter = painter,
        contentDescription = contentDescription,
        colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface),
        onClick = onClick,
    )
}
