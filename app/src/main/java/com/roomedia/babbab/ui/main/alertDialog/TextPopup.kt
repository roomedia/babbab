package com.roomedia.babbab.ui.main.alertDialog

import androidx.annotation.StringRes
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import com.roomedia.babbab.ui.main.button.BorderlessTextButton

@Composable
fun TextPopup(
    showDialog: MutableState<Boolean>,
    onConfirm: () -> Unit,
    title: String,
) {
    if (showDialog.value.not()) return
    AlertDialog(
        onDismissRequest = {
            showDialog.value = false
        },
        confirmButton = {
            BorderlessTextButton(text = "✔️") {
                onConfirm()
                showDialog.value = false
            }
        },
        dismissButton = {
            BorderlessTextButton(text = "❌️") {
                showDialog.value = false
            }
        },
        title = {
            Text(title)
        },
    )
}

@Composable
fun TextPopup(
    showDialog: MutableState<Boolean>,
    onConfirm: () -> Unit,
    @StringRes titleId: Int,
) {
    TextPopup(
        showDialog = showDialog,
        onConfirm = onConfirm,
        title = stringResource(titleId),
    )
}
