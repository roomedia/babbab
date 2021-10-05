package com.roomedia.babbab

import android.app.Application
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import timber.log.Timber

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Firebase.auth.signOut()
            Firebase.auth.useEmulator("10.0.2.2", 9099)
            Firebase.database.useEmulator("10.0.2.2", 9000)
            Firebase.messaging.token.addOnCompleteListener { task ->
                if (task.isSuccessful.not()) {
                    Timber.w("Fetching FCM registration token failed: ${task.exception}")
                    return@addOnCompleteListener
                }
                Timber.d(task.result)
            }
        }
    }
}