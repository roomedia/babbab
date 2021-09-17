package com.roomedia.babbab.ui.main

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.roomedia.babbab.BuildConfig
import com.roomedia.babbab.R
import com.roomedia.babbab.databinding.ActivityMainBinding
import com.roomedia.babbab.databinding.PopupSendPreviewBinding
import com.roomedia.babbab.model.DeviceNotificationModel
import com.roomedia.babbab.model.NotificationModel
import com.roomedia.babbab.service.ApiClient
import com.roomedia.babbab.ui.login.LoginActivity
import com.roomedia.babbab.ui.main.ui.theme.BabbabTheme
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

        binding.buttonSendQuestion.setOnClickListener {
            showQuestionPopup()
        }
        binding.buttonSendAnswer.setOnClickListener {
            takePictureAndShowAnswerPopupLauncher.launch(latestTmpUri)
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

    private fun showQuestionPopup() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.question_text))
            .setNegativeButton("❌", null)
            .setPositiveButton("✔️") { _, _ ->
                sendQuestion()
            }
            .show()
    }

    private fun sendQuestion() {
        val model = DeviceNotificationModel(
            // TODO : change to device group token
            "eVDowI2PRF-T6C3_XfhXNB:APA91bHW2noxUWHeVVgKOBO31L5IZYPenVIb6UDLd7r657ro6Zh08rSsbf-TRSeXFhbxVfSLieNk4Q3zYEYt7St-Rr3D0-kI4nGf_Xuu9T5Q_2aa736DsVlTduW0WcgZTW0Srdl_kh2a",
            NotificationModel(
                "question from ${Firebase.auth.currentUser?.displayName}",
                getString(R.string.question_text)
            )
        )
        lifecycleScope.launch(Dispatchers.IO) {
            ApiClient.messageService.sendNotification(model)
        }
    }

    private fun showAnswerPopupIfSuccess(isSuccess: Boolean) {
        if (isSuccess) {
            showAnswerPopup()
        }
    }

    private fun showAnswerPopup() {
        val binding = PopupSendPreviewBinding.inflate(layoutInflater)
        binding.imagePreview.setImageURI(latestTmpUri)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.answer_text))
            .setView(binding.root)
            .setNegativeButton("❌", null)
            .setPositiveButton("✔️") { _, _ ->
                sendAnswer(latestTmpUri)
            }
            .setCancelable(false)
            .show()
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
                    "answer from ${Firebase.auth.currentUser?.displayName}",
                    getString(R.string.answer_text),
                    ApiClient.imageUploadService.upload(image).data.medium.url
                )
            )
            ApiClient.messageService.sendNotification(deviceNotificationModel)
        }
    }
}

@Composable
fun SendNotificationButtons(
    showQuestionPopup: () -> Unit = {},
    showAnswerPopup: () -> Unit = {},
) {
    ConstraintLayout(Modifier.fillMaxHeight()) {
        Row(
            modifier = Modifier.padding(8.dp).constrainAs(createRef()) {
                bottom.linkTo(parent.bottom)
            },
        ) {
            Button(showQuestionPopup, text = "🤔🍚")
            Spacer(Modifier.width(4.dp))
            Button(showAnswerPopup, text = "🤤🍚")
        }
    }
}

@Composable
fun RowScope.Button(onClick: () -> Unit, modifier: Modifier = Modifier, text: String) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .weight(1f)
            .height(72.dp)
            .then(modifier),
    ) {
        Text(text, fontSize = 28.sp)
    }
}

@Preview(
    name = "Light Theme",
    showBackground = true,
)
@Preview(
    name = "Dark Theme",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES,
)
@Composable
fun DefaultPreview() {
    BabbabTheme {
        Scaffold {
            SendNotificationButtons()
        }
    }
}
