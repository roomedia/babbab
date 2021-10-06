package com.roomedia.babbab.ui.main.screen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.roomedia.babbab.R
import com.roomedia.babbab.ui.main.button.SettingTextButton

interface Settings {
    @Composable
    fun AppCompatActivity.Settings() {
        Column {
            SettingTextButton(
                textId = R.string.open_source_licenses,
                onClick = {
                    OssLicensesMenuActivity.setActivityTitle(getString(R.string.open_source_licenses))
                    startActivity(Intent(baseContext, OssLicensesMenuActivity::class.java))
                },
            )
        }
    }
}
