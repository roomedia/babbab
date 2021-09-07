package com.roomedia.babbab

import android.app.Application
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())

            // When running in debug mode, connect to the Firebase Emulator Suite.
            // "10.0.2.2" is a special IP address which allows the Android Emulator
            // to connect to "localhost" on the host computer. The port values (9xxx)
            // must match the values defined in the firebase.json file.
            Firebase.auth.useEmulator("10.0.2.2", 9099)
        }
    }
}