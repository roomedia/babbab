package com.roomedia.babbab.ui.main.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberImagePainter
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.roomedia.babbab.BuildConfig
import com.roomedia.babbab.R
import com.roomedia.babbab.service.BabbabPreferences
import com.roomedia.babbab.ui.login.LoginActivity
import com.roomedia.babbab.ui.main.button.BorderlessTextButton
import com.roomedia.babbab.ui.main.button.SettingTextButton
import com.roomedia.babbab.ui.main.text.SectionText
import timber.log.Timber
import java.util.*

interface Settings {

    val currentUser get() = Firebase.auth.currentUser
    val settingsScreenTime: MutableState<Long>

    @Composable
    fun Activity.ProfileSection() {
        Spacer(Modifier.height(32.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val painter = if (currentUser?.photoUrl == null) {
                painterResource(R.drawable.ic_launcher_background)
            } else {
                rememberImagePainter(
                    data = currentUser?.photoUrl,
                    builder = {
                        crossfade(true)
                    },
                )
            }
            Image(
                painter = painter,
                contentDescription = currentUser?.displayName?.let {
                    stringResource(R.string.profile_image, it)
                },
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(50)),
            )
            Spacer(Modifier.height(16.dp))
            BorderlessTextButton("✏️") {
                currentUser?.updateProfile(
                    UserProfileChangeRequest.Builder()
                        .setPhotoUri(Uri.parse("Url"))
                        .build()
                )
                settingsScreenTime.value = Date().time
            }
        }
        SectionText(textId = R.string.profile_settings)
        SettingTextButton(
            text = stringResource(R.string.display_name),
            subtext = currentUser?.displayName,
            onClick = {
                currentUser?.updateProfile(
                    UserProfileChangeRequest.Builder()
                        .setDisplayName(settingsScreenTime.value.toString())
                        .build()
                )
                settingsScreenTime.value = Date().time
            },
        )
        SettingTextButton(
            text = stringResource(R.string.email),
            subtext = currentUser?.email,
            showArrow = false,
        )
        SettingTextButton(
            textId = R.string.log_out,
            onClick = {
                Firebase.auth.signOut()
                startActivity(LoginActivity.createIntent(this))
                finish()
            },
        )
        Divider()
    }

    fun Context.launchUrl(url: String) {
        CustomTabsIntent.Builder()
            .build()
            .launchUrl(this, Uri.parse(url))
    }

    @Composable
    fun ImageExpirationSpinnerButton() {
        val showDialog = remember { mutableStateOf(false) }
        SettingTextButton(
            text = stringResource(R.string.auto_delete_image),
            subtext = BabbabPreferences.getImageExpiration().second,
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
    fun Activity.ImageSettingsSection() {
        SectionText(textId = R.string.image_settings)
        ImageExpirationSpinnerButton()
        SettingTextButton(
            textId = R.string.terms_of_service,
            onClick = { launchUrl("https://imgbb.com/tos") },
        )
        SettingTextButton(
            textId = R.string.privacy,
            onClick = { launchUrl("https://imgbb.com/privacy") },
        )
        Text(
            text = stringResource(R.string.image_caution),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    top = 8.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                ),
            color = MaterialTheme.colors.onBackground.copy(0.7f),
            fontSize = 12.sp,
        )
        Divider()
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
    fun Activity.AppInfoSection() {
        SectionText(textId = R.string.app_info)
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
        Divider()
    }

    @Composable
    fun Activity.AccountInfoSection() {
        SectionText(textId = R.string.account)
        SettingTextButton(
            textId = R.string.delete_account,
            onClick = {
                Firebase.auth.currentUser?.delete()?.addOnSuccessListener {
                    startActivity(LoginActivity.createIntent(this))
                    finish()
                }
            },
        )
    }

    @Composable
    fun Activity.Settings() {
        Timber.d("Recomposing settings - ${settingsScreenTime.value}")
        Column(Modifier.verticalScroll(rememberScrollState())) {
            ProfileSection()
            ImageSettingsSection()
            AppInfoSection()
            AccountInfoSection()
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
