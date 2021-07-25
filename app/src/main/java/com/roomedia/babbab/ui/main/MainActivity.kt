package com.roomedia.babbab.ui.main

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.roomedia.babbab.BuildConfig
import com.roomedia.babbab.R
import com.roomedia.babbab.databinding.ActivityMainBinding
import com.roomedia.babbab.databinding.PopupSendPreviewBinding
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
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.tag("TAG").w(task.exception, "Fetching FCM registration token failed")
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = getString(R.string.msg_token_fmt, token)
            Timber.d(msg)
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })
    }

    private fun showSendQuestionPopup() {
        AlertDialog.Builder(this)
            .setTitle("💭🍚🥯🧇🍟🍜🍛❓❓❓️")
            .setNegativeButton("❌", null)
            .setPositiveButton("✔️") { _, _ ->
                // TODO: send picture notification using firebase
            }
            .show()
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
            .setTitle("🍚🥯🧇🍟🍜🍛")
            .setView(binding.root)
            .setNegativeButton("❌", null)
            .setPositiveButton("✔️") { _, _ ->
                // TODO: send picture notification using firebase
            }
            .setCancelable(false)
            .show()
    }
}