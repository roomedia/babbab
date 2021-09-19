package com.roomedia.babbab.ui.main

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
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
import com.roomedia.babbab.ui.theme.BabbabTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File

class MainActivity : AppCompatActivity() {

    private val latestTmpUri by lazy { getTmpFileUri() }
    private val takePictureAndShowAnswerPopupLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture(), ::showAnswerPopupIfSuccess)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Firebase.auth.currentUser == null) {
            startActivity(LoginActivity.createIntent(this))
            finish()
            return
        }

        setContent {
            BabbabTheme {
                Scaffold {
                    NotificationList()
                    SendNotificationButtons()
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
            ApiClient.messageService.sendNotification(model)
        }
    }

    private fun sendAnswer(imageUri: Uri) {
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
fun SendNotificationButtons(
    sendQuestion: () -> Unit = {},
    sendAnswer: () -> Unit = {},
) {
    ConstraintLayout(Modifier.fillMaxHeight()) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .constrainAs(createRef()) {
                    bottom.linkTo(parent.bottom)
                },
        ) {
            TextButton(text = "🤔🍚") { showQuestionPopup.value = true }
            Spacer(Modifier.width(4.dp))
            TextButton(text = "🤤🍚") { showAnswerPopup.value = true }
        }
    }
}

@Preview(name = "Light Theme",)
@Preview(
    name = "Dark Theme",
    uiMode = UI_MODE_NIGHT_YES,
)
@Composable
fun DefaultPreview() {
    BabbabTheme {
        Scaffold {
            NotificationList()
            SendNotificationButtons()
        }
    }
}
