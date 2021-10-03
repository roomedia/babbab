package com.roomedia.babbab.ui.main.screen

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.roomedia.babbab.BuildConfig
import com.roomedia.babbab.R
import com.roomedia.babbab.model.DeviceNotificationModel
import com.roomedia.babbab.model.NotificationChannelEnum
import com.roomedia.babbab.service.ApiClient
import com.roomedia.babbab.ui.main.alertDialog.AnswerPopup
import com.roomedia.babbab.ui.main.alertDialog.QuestionPopup
import com.roomedia.babbab.ui.main.notificationList.NotificationList
import com.roomedia.babbab.ui.main.notificationList.SendNotificationButtons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File

interface Home {
    val latestTmpUri: Uri
    val targetUri: MutableState<Uri?>

    val takePhotoLauncher: ActivityResultLauncher<Uri>
    val selectPhotoLauncher: ActivityResultLauncher<String>

    fun setPhotoUri(uri: Uri? = latestTmpUri) {
        if (uri == null) return
        targetUri.value = uri
    }

    fun Context.getTmpUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(
            applicationContext,
            "${BuildConfig.APPLICATION_ID}.provider",
            tmpFile
        )
    }

    fun Context.getRecentPhotoUriList(): List<Uri?> {
        val uriList = mutableListOf<Uri?>()
        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED,
        )
        val sortOrder =
            MediaStore.Images.Media.DATE_ADDED + " DESC LIMIT $RECENT_PHOTO_COUNT"
        contentResolver.query(contentUri, projection, null, null, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                uriList += ContentUris.withAppendedId(contentUri, cursor.getLong(idColumn))
            }
        }
        return uriList + arrayOfNulls(RECENT_PHOTO_COUNT - uriList.size)
    }

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
        QuestionPopup(showQuestionPopup) { sendQuestion() }
        AnswerPopup(
            showDialog = showAnswerPopup,
            sendAnswer = { sendAnswer() },
            takePhoto = { takePhotoLauncher.launch(latestTmpUri) },
            selectPhoto = { selectPhotoLauncher.launch("image/*") },
            recentUris = getRecentPhotoUriList(),
            targetUri = targetUri,
        )
    }

    companion object {
        const val RECENT_PHOTO_COUNT = 2
    }
}
