package com.roomedia.babbab.ui.main

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.roomedia.babbab.BuildConfig
import com.roomedia.babbab.R
import com.roomedia.babbab.databinding.ActivityMainBinding
import com.roomedia.babbab.databinding.PopupSendPreviewBinding
import com.roomedia.babbab.model.DeviceNotificationModel
import com.roomedia.babbab.model.NotificationModel
import com.roomedia.babbab.service.ApiClient
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File

class MainActivity : AppCompatActivity() {

    private var latestTmpUri: Uri? = null
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess.not()) return@registerForActivityResult
        latestTmpUri?.let { uri ->
            showSendPreviewPopup(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonSendQuestion.setOnClickListener {
            showSendQuestionPopup()
        }
        binding.buttonSendPicture.setOnClickListener {
            takeImage()
        }

        if (BuildConfig.DEBUG.not()) return
        Firebase.messaging.token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.w("Fetching FCM registration token failed: ${task.exception}")
                return@addOnCompleteListener
            }
            Timber.d(task.result)
        }
    }

    private fun showSendQuestionPopup() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.question_text))
            .setNegativeButton("❌", null)
            .setPositiveButton("✔️") { _, _ ->
                sendQuestion()
            }
            .show()
    }

    private fun sendQuestion() = CoroutineScope(Dispatchers.IO).launch {
        val model = DeviceNotificationModel(
            // TODO : change to device group token
            "eVDowI2PRF-T6C3_XfhXNB:APA91bHW2noxUWHeVVgKOBO31L5IZYPenVIb6UDLd7r657ro6Zh08rSsbf-TRSeXFhbxVfSLieNk4Q3zYEYt7St-Rr3D0-kI4nGf_Xuu9T5Q_2aa736DsVlTduW0WcgZTW0Srdl_kh2a",
            NotificationModel(
                "question from ${Firebase.auth.currentUser?.displayName}",
                getString(R.string.question_text)
            )
        )
        ApiClient.messageService.sendNotification(model)
    }

    private fun takeImage() {
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                latestTmpUri = uri
                takePictureLauncher.launch(uri)
            }
        }
    }

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(applicationContext, "${BuildConfig.APPLICATION_ID}.provider", tmpFile)
    }

    private fun showSendPreviewPopup(uri: Uri) {
        val binding = PopupSendPreviewBinding.inflate(layoutInflater)
        binding.imagePreview.setImageURI(uri)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.answer_text))
            .setView(binding.root)
            .setNegativeButton("❌", null)
            .setPositiveButton("✔️") { _, _ ->
                sendAnswer(uri.toString())
            }
            .setCancelable(false)
            .show()
    }

    private fun sendAnswer(imageUri: String) = CoroutineScope(Dispatchers.IO).launch {
        val model = DeviceNotificationModel(
            // TODO : change to device group token
            "eVDowI2PRF-T6C3_XfhXNB:APA91bHW2noxUWHeVVgKOBO31L5IZYPenVIb6UDLd7r657ro6Zh08rSsbf-TRSeXFhbxVfSLieNk4Q3zYEYt7St-Rr3D0-kI4nGf_Xuu9T5Q_2aa736DsVlTduW0WcgZTW0Srdl_kh2a",
            NotificationModel(
                "answer from ${Firebase.auth.currentUser?.displayName}",
                getString(R.string.answer_text),
                imageUri
            )
        )
        ApiClient.messageService.sendNotification(model)
    }
}