package com.roomedia.babbab.extension

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher

fun Context.startActivity(`class`: Class<*>) {
    startActivity(Intent(this, `class`))
}

fun <T> T.startActivityForResult(launcher: ActivityResultLauncher<T>) {
    launcher.launch(this)
}