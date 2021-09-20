package com.roomedia.babbab.ui.main.alertDialog

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
import com.roomedia.babbab.R
import com.roomedia.babbab.ui.main.button.BorderlessTextButton

@Composable
fun QuestionPopup(showDialog: MutableState<Boolean>, sendQuestion: () -> Unit) {
    if (showDialog.value.not()) return
    AlertDialog(
        onDismissRequest = {
            showDialog.value = false
        },
        buttons = {
            Row(
                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                BorderlessTextButton(text = "❌️") {
                    showDialog.value = false
                }
                BorderlessTextButton(text = "✔️") {
                    sendQuestion()
                    showDialog.value = false
                }
            }
        },
        title = { Text(stringResource(R.string.question_text)) },
    )
}
