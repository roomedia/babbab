package com.roomedia.babbab.service

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.roomedia.babbab.R

object BabbabPreferences {
    private lateinit var _appContext: Context
    val appContext get() = _appContext

    private lateinit var _preference: SharedPreferences
    val preference get() = _preference

    fun init(appContext: Context) {
        _appContext = appContext
        _preference = PreferenceManager.getDefaultSharedPreferences(appContext)
    }

    private const val KEY_IMAGE_EXPIRATION = "key_image_expiration"
    private const val KEY_IMAGE_EXPIRATION_DESCRIPTION = "key_image_expiration_description"
    fun getImageExpiration(): Pair<Int, String> {
        return Pair(
            preference.getInt(KEY_IMAGE_EXPIRATION, 259200),
            preference.getString(KEY_IMAGE_EXPIRATION_DESCRIPTION, null)
                ?: appContext.getString(R.string.time_after, 3, appContext.getString(R.string.day))
        )
    }
    fun setImageExpiration(seconds: Int, description: String) {
        preference.edit()
            .putInt(KEY_IMAGE_EXPIRATION, seconds)
            .putString(KEY_IMAGE_EXPIRATION_DESCRIPTION, description)
            .apply()
    }
}