package com.roomedia.babbab.ui.login

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.roomedia.babbab.R
import com.roomedia.babbab.extension.startActivityForResult
import com.roomedia.babbab.model.User
import timber.log.Timber

class LoginActivity : AppCompatActivity() {

    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract(), ::onSignInResult)

    override fun onStart() {
        super.onStart()
        if (Firebase.auth.currentUser == null) {
            startSignInActivityForResult()
        } else {
            startPreviousActivity()
        }
    }

    private fun startSignInActivityForResult() {
        AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setLogo(R.mipmap.ic_launcher)
            .setAvailableProviders(listOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build(),
                AuthUI.IdpConfig.TwitterBuilder().build(),
                // TODO: Add Facebook App json,
//            AuthUI.IdpConfig.FacebookBuilder().build(),
                // TODO: Add Naver Login,
                // TODO: Add Kakao Login,
            ))
            .build()
            .startActivityForResult(signInLauncher)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == RESULT_OK) {
            Timber.d("Sign in successful!")
            Firebase.auth.currentUser?.apply {
                insertUserToFirebaseRealtimeDatabase(uid, displayName, email)
            }
            startPreviousActivity()
        } else {
            val response = result.idpResponse
            if (response == null) {
                Timber.w("Sign in canceled")
            } else {
                Timber.w("Sign in error", response.error)
            }
            Toast.makeText(this, R.string.sign_in_error, Toast.LENGTH_LONG).show()
        }
    }

    private fun insertUserToFirebaseRealtimeDatabase(uid: String, displayName: String?, email: String?) {
        val user = User(displayName, email)
        Firebase.database.getReference("user").child(uid).setValue(user).addOnSuccessListener {
            Timber.d("Success to insert user")
        }.addOnFailureListener {
            Timber.d("Fail to insert user: $it")
        }
    }

    private fun startPreviousActivity() {
        intent.getStringExtra(KEY_ACTIVITY_NAME)?.also { activityName ->
            startActivity(Intent(this, Class.forName(activityName)))
        }
        finish()
    }

    companion object {
        private const val KEY_ACTIVITY_NAME = "activity_name"

        fun createIntent(activity: Activity): Intent =
            Intent(activity, LoginActivity::class.java)
                .putExtra(KEY_ACTIVITY_NAME, activity::class.qualifiedName)
    }
}