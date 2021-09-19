package com.roomedia.babbab.ui.main.alertDialog

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.roomedia.babbab.ui.main.button.BorderlessTextButton

@Composable
fun BasePopup(
    showDialog: MutableState<Boolean>,
    onDismissRequest: (() -> Unit)? = null,
    onClickConfirm: () -> Unit,
    @StringRes titleRes: Int,
    properties: DialogProperties = DialogProperties(),
    content: (@Composable () -> Unit)? = null
) {
    if (showDialog.value.not()) return
    AlertDialog(
        onDismissRequest = {
            showDialog.value = false
            onDismissRequest?.invoke()
        },
        buttons = {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                BorderlessTextButton(text = "❌️") {
                    showDialog.value = false
                    onDismissRequest?.invoke()
                }
                BorderlessTextButton(text = "✔️") {
                    onClickConfirm()
                    showDialog.value = false
                    onDismissRequest?.invoke()
                }
            }
        },
        title = { Text(stringResource(titleRes)) },
        text = { content?.invoke() },
        properties = properties,
    )
}
