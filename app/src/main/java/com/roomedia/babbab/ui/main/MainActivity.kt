package com.roomedia.babbab.ui.main

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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