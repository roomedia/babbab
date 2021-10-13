package com.roomedia.babbab.ui.main.screen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.roomedia.babbab.R
import com.roomedia.babbab.model.DeviceNotificationModel
import com.roomedia.babbab.model.NotificationChannelEnum
import com.roomedia.babbab.service.ApiClient
import com.roomedia.babbab.ui.main.alertDialog.ImagePopupInterface
import com.roomedia.babbab.ui.main.alertDialog.TextPopup
import com.roomedia.babbab.ui.main.notificationList.NotificationList
import com.roomedia.babbab.ui.main.notificationList.SendNotificationButtons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.ByteArrayOutputStream

interface Home : ImagePopupInterface {

    fun AppCompatActivity.sendQuestion() {
        val model = DeviceNotificationModel(
            // TODO : change to device group token
            to = "eVDowI2PRF-T6C3_XfhXNB:APA91bHW2noxUWHeVVgKOBO31L5IZYPenVIb6UDLd7r657ro6Zh08rSsbf-TRSeXFhbxVfSLieNk4Q3zYEYt7St-Rr3D0-kI4nGf_Xuu9T5Q_2aa736DsVlTduW0WcgZTW0Srdl_kh2a",
            channelId = NotificationChannelEnum.Question.id,
            title = getString(R.string.question_from, Firebase.auth.currentUser?.displayName),
            body = getString(R.string.question_text),
        )
        lifecycleScope.launch(Dispatchers.IO) {
            val result = ApiClient.messageService.sendNotification(model)
            Timber.d("$result")
        }
    }

    fun AppCompatActivity.sendAnswer() {
        val imageUri = targetUri.value ?: run {
            Toast.makeText(this, R.string.send_answer_error, Toast.LENGTH_LONG).show()
            return
        }
        val image = contentResolver.openInputStream(imageUri)
            .run { BitmapFactory.decodeStream(this) }
            .run {
                ByteArrayOutputStream()
                    .apply { compress(Bitmap.CompressFormat.JPEG, 100, this) }
                    .toByteArray()
            }
            .run { Base64.encodeToString(this, Base64.DEFAULT) }

        lifecycleScope.launch(Dispatchers.IO) {
            val deviceNotificationModel = DeviceNotificationModel(
                // TODO : change to device group token
                to = "eVDowI2PRF-T6C3_XfhXNB:APA91bHW2noxUWHeVVgKOBO31L5IZYPenVIb6UDLd7r657ro6Zh08rSsbf-TRSeXFhbxVfSLieNk4Q3zYEYt7St-Rr3D0-kI4nGf_Xuu9T5Q_2aa736DsVlTduW0WcgZTW0Srdl_kh2a",
                channelId = NotificationChannelEnum.Answer.id,
                title = getString(R.string.answer_from, Firebase.auth.currentUser?.displayName),
                body = getString(R.string.answer_text),
                image = ApiClient.imageUploadService.upload(image).data.medium.url,
            )
            ApiClient.messageService.sendNotification(deviceNotificationModel)
        }
    }

    @Composable
    fun AppCompatActivity.Home() {
        val showQuestionPopup = remember { mutableStateOf(false) }
        val showAnswerPopup = remember { mutableStateOf(false) }

        NotificationList()
        SendNotificationButtons(showQuestionPopup, showAnswerPopup)
        TextPopup(
            showDialog = showQuestionPopup,
            onConfirm = { sendQuestion() },
            titleId = R.string.question_text,
        )
        ImagePopup(
            showDialog = showAnswerPopup,
            onConfirm = { sendAnswer() },
            titleId = R.string.answer_text,
        )
    }
}
