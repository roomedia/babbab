package com.roomedia.babbab.ui.main.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.roomedia.babbab.BuildConfig
import com.roomedia.babbab.R
import com.roomedia.babbab.service.BabbabPreferences
import com.roomedia.babbab.ui.main.button.SettingTextButton

interface Settings {

    fun Context.launchUrl(url: String) {
        CustomTabsIntent.Builder()
            .build()
            .launchUrl(this, Uri.parse(url))
    }

    @Composable
    fun ImageExpirationSpinnerButton() {
        val showDialog = remember { mutableStateOf(false) }
        SettingTextButton(
            text = stringResource(
                R.string.auto_delete_image,
                BabbabPreferences.getImageExpiration().second,
            ),
            onClick = {
                showDialog.value = true
            },
        )
        if (showDialog.value) {
            Dialog(onDismissRequest = { showDialog.value = false }) {
                Card {
                    Column {
                        EXPIRATION.forEach { (amount, unit) ->
                            val description = stringResource(
                                R.string.time_after,
                                amount,
                                stringResource(unit.description),
                            )
                            SettingTextButton(
                                text = description,
                                onClick = {
                                    showDialog.value = false
                                    BabbabPreferences.setImageExpiration(
                                        amount * unit.seconds,
                                        description
                                    )
                                },
                                showArrow = false,
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun Activity.CheckForUpdateButton() {
        val isLatestVersion = remember { mutableStateOf(true) }
        val appUpdateInfoState: MutableState<AppUpdateInfo?> = remember { mutableStateOf(null) }

        val appUpdateManager = AppUpdateManagerFactory.create(this).apply {
            appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                isLatestVersion.value =
                    appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                appUpdateInfoState.value = appUpdateInfo
            }
        }

        if (isLatestVersion.value) {
            SettingTextButton(
                text = stringResource(R.string.current_version, BuildConfig.VERSION_NAME),
                showArrow = false,
            )
        } else {
            SettingTextButton(
                textId = R.string.update_application,
                onClick = {
                    val appUpdateInfo = appUpdateInfoState.value
                    if (appUpdateInfo?.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) == true) {
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
            SettingTextButton(textId = R.string.image_settings, showArrow = false)
            ImageExpirationSpinnerButton()
            SettingTextButton(
                textId = R.string.terms_of_service,
                onClick = { launchUrl("https://imgbb.com/tos") },
            )
            SettingTextButton(
                textId = R.string.privacy,
                onClick = { launchUrl("https://imgbb.com/privacy") },
            )
            SettingTextButton(textId = R.string.image_caution, showArrow = false)
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

        enum class TimeUnit(val seconds: Int, @StringRes val description: Int) {
            MINUTE(60, R.string.minute),
            HOUR(3600, R.string.hour),
            DAY(86400, R.string.day),
            WEEK(604800, R.string.week),
        }
        val EXPIRATION = listOf(
            Pair(15, TimeUnit.MINUTE),
            Pair(30, TimeUnit.MINUTE),
            Pair(1, TimeUnit.HOUR),
            Pair(6, TimeUnit.HOUR),
            Pair(12, TimeUnit.HOUR),
            Pair(1, TimeUnit.DAY),
            Pair(3, TimeUnit.DAY),
            Pair(1, TimeUnit.WEEK),
            Pair(4, TimeUnit.WEEK),
        )
    }
}
