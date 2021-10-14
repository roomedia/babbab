package com.roomedia.babbab.ui.main.alertDialog

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil.compose.rememberImagePainter
import com.roomedia.babbab.BuildConfig
import com.roomedia.babbab.R
import com.roomedia.babbab.ui.main.button.BorderlessTextButton
import com.roomedia.babbab.ui.main.button.ImageButton
import com.roomedia.babbab.ui.main.button.ThumbnailButton
import com.roomedia.babbab.ui.theme.Shapes
import com.roomedia.babbab.util.checkSelfPermissionCompat
import com.roomedia.babbab.util.requestPermissionsCompat
import timber.log.Timber
import java.io.File

interface ImagePopupInterface {

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

    fun AppCompatActivity.getRecentPhotoUriList(): List<Uri?> {
        val uriList = mutableListOf<Uri?>()
        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED,
        )
        val sortOrder =
            MediaStore.Images.Media.DATE_ADDED + " DESC LIMIT $RECENT_PHOTO_COUNT"
        try {
            contentResolver.query(contentUri, projection, null, null, sortOrder)?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                while (cursor.moveToNext()) {
                    uriList += ContentUris.withAppendedId(contentUri, cursor.getLong(idColumn))
                }
            }
        } catch (exception: Exception) {
            if (checkSelfPermissionCompat(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                requestPermissionsCompat(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_READ_STORAGE
                )
            } else {
                Timber.e(exception)
            }
        }
        return uriList + arrayOfNulls(RECENT_PHOTO_COUNT - uriList.size)
    }

    fun Context.onRequestReadStoragePermissionsResult(requestCode: Int, grantResults: IntArray) {
        when {
            requestCode != PERMISSION_REQUEST_READ_STORAGE -> return
            grantResults.all { it == PackageManager.PERMISSION_GRANTED } -> return
            else -> {
                Toast.makeText(this, R.string.read_storage_permission_denied, Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    @Composable
    fun AppCompatActivity.ImagePopupContent() {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ImageButton(
                    painter = painterResource(R.drawable.ic_take_photo),
                    contentDescription = stringResource(R.string.take_photo),
                    onClick = {
                        takePhotoLauncher.launch(latestTmpUri)
                    },
                )
                Spacer(Modifier.width(8.dp))
                ImageButton(
                    painter = painterResource(R.drawable.ic_select_photo),
                    contentDescription = stringResource(R.string.select_photo),
                    onClick = { selectPhotoLauncher.launch("image/*") },
                )

                getRecentPhotoUriList().forEachIndexed { idx, uri ->
                    Spacer(Modifier.width(8.dp))
                    if (uri == null) {
                        Spacer(Modifier.weight(1f))
                    } else {
                        ThumbnailButton(
                            painter = rememberImagePainter(uri, builder = {
                                crossfade(true)
                            }),
                            contentDescription = stringResource(R.string.recent_photo, idx),
                            onClick = { targetUri.value = uri },
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            // TODO: Change to Guide Image
            val painter = if (targetUri.value == null) {
                painterResource(R.drawable.ic_launcher_background)
            } else {
                rememberImagePainter(targetUri.value, builder = {
                    crossfade(true)
                })
            }
            Image(
                painter = painter,
                contentDescription = targetUri.value.toString(),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(Shapes.medium),
                contentScale = ContentScale.Crop,
            )
        }
    }

    @Composable
    fun AppCompatActivity.ImagePopup(
        showDialog: MutableState<Boolean>,
        onConfirm: () -> Unit,
        title: String,
    ) {
        if (showDialog.value.not()) return
        AlertDialog(
            onDismissRequest = {
                targetUri.value = null
                showDialog.value = false
            },
            confirmButton = {
                BorderlessTextButton(text = "✔️") {
                    onConfirm()
                    targetUri.value = null
                    showDialog.value = false
                }
            },
            dismissButton = {
                BorderlessTextButton(text = "❌️") {
                    targetUri.value = null
                    showDialog.value = false
                }
            },
            title = { Text(title) },
            text = { ImagePopupContent() },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
        )
    }

    @Composable
    fun AppCompatActivity.ImagePopup(
        showDialog: MutableState<Boolean>,
        onConfirm: () -> Unit,
        @StringRes titleId: Int,
    ) {
        ImagePopup(
            showDialog = showDialog,
            onConfirm = onConfirm,
            title = stringResource(titleId),
        )
    }

    companion object {
        const val RECENT_PHOTO_COUNT = 2
        const val PERMISSION_REQUEST_READ_STORAGE = 0
    }
}