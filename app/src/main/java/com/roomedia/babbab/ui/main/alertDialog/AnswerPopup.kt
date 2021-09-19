package com.roomedia.babbab.ui.main.alertDialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.window.DialogProperties
import com.roomedia.babbab.R

@Composable
fun AnswerPopup(showDialog: MutableState<Boolean>, sendAnswer: () -> Unit, takePhoto: () -> Unit, selectPhoto: () -> Unit) {
    BasePopup(
        showDialog = showDialog,
        onClickConfirm = sendAnswer,
        titleRes = R.string.answer_text,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
        content = { AnswerPopupContent(takePhoto, selectPhoto) },
    )
}
