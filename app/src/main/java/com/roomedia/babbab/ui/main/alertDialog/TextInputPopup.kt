package com.roomedia.babbab.ui.main.alertDialog

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.roomedia.babbab.ui.main.button.BorderlessTextButton

@Composable
fun TextInputPopup(
    showDialog: MutableState<Boolean>? = null,
    onConfirm: (String) -> Unit,
    onDismiss: (() -> Unit)? = null,
    title: String,
    textFieldState: MutableState<String>,
) {
    if (showDialog?.value == false) return
    AlertDialog(
        onDismissRequest = {
            onDismiss?.invoke()
            showDialog?.value = false
        },
        confirmButton = {
            BorderlessTextButton(text = "✔️") {
                onConfirm(textFieldState.value)
                showDialog?.value = false
            }
        },
        dismissButton = {
            BorderlessTextButton(text = "❌️") {
                onDismiss?.invoke()
                showDialog?.value = false
            }
        },
        title = {
            Text(title)
        },
        text = {
            OutlinedTextField(
                value = textFieldState.value,
                onValueChange = { textFieldState.value = it },
                trailingIcon = {
                    if (textFieldState.value == "") return@OutlinedTextField
                    IconButton(
                        onClick = { textFieldState.value = "" },
                        content = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear Message Text",
                                modifier = Modifier.padding(15.dp).size(24.dp),
                            )
                        },
                    )
                },
            )
        },
    )
}

@Composable
fun TextInputPopup(
    showDialog: MutableState<Boolean>? = null,
    onConfirm: (String) -> Unit,
    onDismiss: (() -> Unit)? = null,
    @StringRes titleId: Int,
    textFieldState: MutableState<String>,
) {
    TextInputPopup(
        showDialog = showDialog,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        title = stringResource(titleId),
        textFieldState = textFieldState,
    )
}
