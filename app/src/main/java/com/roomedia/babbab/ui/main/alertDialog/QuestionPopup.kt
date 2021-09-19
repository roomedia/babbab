package com.roomedia.babbab.ui.main.alertDialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import com.roomedia.babbab.R

@Composable
fun QuestionPopup(showDialog: MutableState<Boolean>, sendQuestion: () -> Unit) {
    BasePopup(
        showDialog = showDialog,
        onClickConfirm = sendQuestion,
        titleRes = R.string.question_text,
    )
}
