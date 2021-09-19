package com.roomedia.babbab.ui.main.alertDialog

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.window.DialogProperties
import com.roomedia.babbab.R

@Composable
fun AnswerPopup(
    showDialog: MutableState<Boolean>,
    sendAnswer: () -> Unit,
    takePhoto: () -> Unit,
    selectPhoto: () -> Unit,
    targetUri: MutableState<Uri?>,
) {
    BasePopup(
        showDialog = showDialog,
        onDismissRequest = {
            targetUri.value = null
        },
        onClickConfirm = sendAnswer,
        titleRes = R.string.answer_text,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
        content = { AnswerPopupContent(takePhoto, selectPhoto, targetUri) },
    )
}
