package com.roomedia.babbab.ui.main.alertDialog

import android.net.Uri
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
import com.roomedia.babbab.R
import com.roomedia.babbab.ui.main.button.BorderlessTextButton

@Composable
fun AnswerPopup(
    showDialog: MutableState<Boolean>,
    sendAnswer: () -> Unit,
    takePhoto: () -> Unit,
    selectPhoto: () -> Unit,
    recentUris: List<Uri?>,
    targetUri: MutableState<Uri?>,
) {
    if (showDialog.value.not()) return
    AlertDialog(
        onDismissRequest = {
            targetUri.value = null
            showDialog.value = false
        },
        buttons = {
            Row(
                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                BorderlessTextButton(text = "❌️") {
                    targetUri.value = null
                    showDialog.value = false
                }
                BorderlessTextButton(text = "✔️") {
                    sendAnswer()
                    targetUri.value = null
                    showDialog.value = false
                }
            }
        },
        title = { Text(stringResource(R.string.answer_text)) },
        text = {
            AnswerPopupContent(
                takePhoto = takePhoto,
                selectPhoto = selectPhoto,
                recentUris = recentUris,
                targetUri = targetUri,
            )
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    )
}
