package com.roomedia.babbab.ui.main.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.roomedia.babbab.BuildConfig
import com.roomedia.babbab.R
import com.roomedia.babbab.ui.main.button.SettingTextButton

interface Settings {

    fun Context.launchUrl(url: String) {
        CustomTabsIntent.Builder()
            .build()
            .launchUrl(this, Uri.parse(url))
    }

    @Composable
    fun Activity.CheckForUpdateButton() {
        val isLatestVersion = remember { mutableStateOf(true) }
        val appUpdateManager = AppUpdateManagerFactory.create(this).apply {
            appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                isLatestVersion.value =
                    appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            }
        }
        val appUpdateInfo = appUpdateManager.appUpdateInfo.result

        if (isLatestVersion.value) {
            SettingTextButton(text = stringResource(R.string.current_version, BuildConfig.VERSION_NAME))
        } else {
            SettingTextButton(
                textId = R.string.update_application,
                onClick = {
                    if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            this,
                            APP_UPDATE_REQUEST_CODE
                        )
                    } else {
                        launchUrl("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
                    }
                },
            )
        }
    }

    @Composable
    fun AppCompatActivity.Settings() {
        Column {
            SettingTextButton(textId = R.string.image_settings)
            SettingTextButton(
                text = stringResource(R.string.auto_delete_image, "After 3 days"),
                onClick = {
                },
            )
            SettingTextButton(
                textId = R.string.terms_of_service,
                onClick = { launchUrl("https://imgbb.com/tos") },
            )
            SettingTextButton(
                textId = R.string.privacy,
                onClick = { launchUrl("https://imgbb.com/privacy") },
            )
            Divider()
            CheckForUpdateButton()
            SettingTextButton(
                textId = R.string.send_feedback,
                onClick = { launchUrl("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}") },
            )
            SettingTextButton(
                textId = R.string.open_source_licenses,
                onClick = {
                    OssLicensesMenuActivity.setActivityTitle(getString(R.string.open_source_licenses))
                    startActivity(Intent(baseContext, OssLicensesMenuActivity::class.java))
                },
            )
        }
    }

    companion object {
        const val APP_UPDATE_REQUEST_CODE = 0
    }
}
