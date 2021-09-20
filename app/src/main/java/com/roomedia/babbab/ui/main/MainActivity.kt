package com.roomedia.babbab.ui.main

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.roomedia.babbab.BuildConfig
import com.roomedia.babbab.R
import com.roomedia.babbab.model.DeviceNotificationModel
import com.roomedia.babbab.model.Event
import com.roomedia.babbab.model.NotificationModel
import com.roomedia.babbab.service.ApiClient
import com.roomedia.babbab.ui.login.LoginActivity
import com.roomedia.babbab.ui.main.alertDialog.AnswerPopup
import com.roomedia.babbab.ui.main.alertDialog.QuestionPopup
import com.roomedia.babbab.ui.main.button.FullWeightTextButton
import com.roomedia.babbab.ui.main.notificationList.NotificationItem
import com.roomedia.babbab.ui.theme.BabbabTheme
import com.roomedia.babbab.util.checkSelfPermissionCompat
import com.roomedia.babbab.util.requestPermissionsCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private val latestTmpUri by lazy { getTmpFileUri() }
    private val targetUri: MutableState<Uri?> = mutableStateOf(null)

    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { setPhotoUri() }
    private val selectPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent(), ::setPhotoUri)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Firebase.auth.currentUser == null) {
            startActivity(LoginActivity.createIntent(this))
            finish()
            return
        }
        setContent {
            val showQuestionPopup = remember { mutableStateOf(false) }
            val showAnswerPopup = remember { mutableStateOf(false) }

            BabbabTheme {
                Scaffold {
                    NotificationList()
                    SendNotificationButtons(showQuestionPopup, showAnswerPopup)
                    QuestionPopup(showQuestionPopup, ::sendQuestion)
                    AnswerPopup(
                        showDialog = showAnswerPopup,
                        sendAnswer = ::sendAnswer,
                        takePhoto = { takePhotoLauncher.launch(latestTmpUri) },
                        selectPhoto = { selectPhotoLauncher.launch("image/*") },
                        recentUris = getRecentPhotoUriList(),
                        targetUri = targetUri,
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (Firebase.auth.currentUser == null) {
            startActivity(LoginActivity.createIntent(this))
            finish()
            return
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_READ_STORAGE -> if (grantResults.any { it == PackageManager.PERMISSION_DENIED }) {
                Toast.makeText(this, "R.string.read_storage_permission_denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getTmpFileUri(): Uri {
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

    private fun setPhotoUri(uri: Uri? = latestTmpUri) {
        if (uri == null) return
        targetUri.value = uri
    }

    private fun getRecentPhotoUriList(): List<Uri?> {
        if (checkSelfPermissionCompat(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            requestPermissionsCompat(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_READ_STORAGE
            )
            return arrayOfNulls<Uri?>(RECENT_PHOTO_COUNT).toList()
        }
        val uriList = mutableListOf<Uri?>()
        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED,
        )
        val sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC LIMIT $RECENT_PHOTO_COUNT"
        contentResolver.query(contentUri, projection, null, null, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                uriList += ContentUris.withAppendedId(contentUri, cursor.getLong(idColumn))
            }
        }
        return uriList + arrayOfNulls(RECENT_PHOTO_COUNT - uriList.size)
    }

    private fun sendQuestion() {
        val model = DeviceNotificationModel(
            // TODO : change to device group token
            "eVDowI2PRF-T6C3_XfhXNB:APA91bHW2noxUWHeVVgKOBO31L5IZYPenVIb6UDLd7r657ro6Zh08rSsbf-TRSeXFhbxVfSLieNk4Q3zYEYt7St-Rr3D0-kI4nGf_Xuu9T5Q_2aa736DsVlTduW0WcgZTW0Srdl_kh2a",
            NotificationModel(
                getString(R.string.question_from, Firebase.auth.currentUser?.displayName),
                getString(R.string.question_text)
            )
        )
        lifecycleScope.launch(Dispatchers.IO) {
            val result = ApiClient.messageService.sendNotification(model)
            Timber.d("$result")
        }
    }

    private fun sendAnswer() {
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
                "eVDowI2PRF-T6C3_XfhXNB:APA91bHW2noxUWHeVVgKOBO31L5IZYPenVIb6UDLd7r657ro6Zh08rSsbf-TRSeXFhbxVfSLieNk4Q3zYEYt7St-Rr3D0-kI4nGf_Xuu9T5Q_2aa736DsVlTduW0WcgZTW0Srdl_kh2a",
                NotificationModel(
                    getString(R.string.answer_from, Firebase.auth.currentUser?.displayName),
                    getString(R.string.answer_text),
                    ApiClient.imageUploadService.upload(image).data.medium.url
                )
            )
            ApiClient.messageService.sendNotification(deviceNotificationModel)
        }
    }

    companion object {
        const val RECENT_PHOTO_COUNT = 2
        const val PERMISSION_REQUEST_READ_STORAGE = 0
    }
}

@Composable
fun NotificationList() {
    LazyColumn(Modifier.padding(bottom = 88.dp)) {
        // TODO: change to real data
        items(10) {
            if (it % 7 == 0) {
                NotificationItem(Event.Question("USER#$it", "yy-MM-dd HH:mm"))
            } else {
                NotificationItem(Event.Answer("User#$it", "yy-MM-dd HH:mm", "https://i.ibb.co/whTbKKD/Eok-Cph-XVQAk-PNw-T.jpg"))
            }
        }
    }
}

@Composable
fun SendNotificationButtons(showQuestionPopup: MutableState<Boolean>, showAnswerPopup: MutableState<Boolean>) {
    ConstraintLayout(Modifier.fillMaxHeight()) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .constrainAs(createRef()) {
                    bottom.linkTo(parent.bottom)
                },
        ) {
            FullWeightTextButton(text = "🤔🍚") { showQuestionPopup.value = true }
            Spacer(Modifier.width(4.dp))
            FullWeightTextButton(text = "🤤🍚") { showAnswerPopup.value = true }
        }
    }
}

@Preview(name = "Light Theme")
@Preview(
    name = "Dark Theme",
    uiMode = UI_MODE_NIGHT_YES,
)
@Composable
fun DefaultPreview() {
    val showQuestionPopup = remember { mutableStateOf(false) }
    val showAnswerPopup = remember { mutableStateOf(false) }

    BabbabTheme {
        Scaffold {
            NotificationList()
            SendNotificationButtons(showQuestionPopup, showAnswerPopup)
        }
    }
}
