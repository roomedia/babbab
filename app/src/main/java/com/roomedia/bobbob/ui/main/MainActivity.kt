package com.roomedia.bobbob.ui.main

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.roomedia.bobbob.BuildConfig
import com.roomedia.bobbob.databinding.ActivityMainBinding
import com.roomedia.bobbob.databinding.PopupSendPreviewBinding
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
            .show()
    }
}