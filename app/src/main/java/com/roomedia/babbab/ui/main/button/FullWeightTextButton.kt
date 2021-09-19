package com.roomedia.babbab.ui.main.button

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RowScope.FullWeightTextButton(text: String, onClick: () -> Unit) {
    BaseTextButton(
        modifier = Modifier.weight(1f).height(72.dp),
        text = text,
        fontSize = 28.sp,
        onClick = onClick,
    )
}
