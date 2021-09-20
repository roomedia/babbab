package com.roomedia.babbab.ui.main.button

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

@Composable
fun RowScope.ThumbnailButton(painter: Painter, contentDescription: String, onClick: () -> Unit) {
    BaseRowImageButton(
        painter = painter,
        contentDescription = contentDescription,
        onClick = onClick,
    )
}
