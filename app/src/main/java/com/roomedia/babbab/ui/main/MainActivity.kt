package com.roomedia.babbab.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.roomedia.babbab.ui.login.LoginActivity
import com.roomedia.babbab.ui.main.button.BorderlessTextButton
import com.roomedia.babbab.ui.main.screen.Home
import com.roomedia.babbab.ui.main.screen.Screen
import com.roomedia.babbab.ui.theme.BabbabTheme
import com.roomedia.babbab.util.checkSelfPermissionCompat
import com.roomedia.babbab.util.requestPermissionsCompat
import timber.log.Timber

class MainActivity : AppCompatActivity(), Home, ActivityCompat.OnRequestPermissionsResultCallback {

    override val latestTmpUri by lazy { getTmpUri() }
    override val targetUri: MutableState<Uri?> = mutableStateOf(null)

    override val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { setPhotoUri() }
    override val selectPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent(), ::setPhotoUri)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Firebase.auth.currentUser == null) {
            startActivity(LoginActivity.createIntent(this))
            finish()
            return
        }
        if (checkSelfPermissionCompat(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            requestPermissionsCompat(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_READ_STORAGE
            )
        }
        setContent {
            val navController = rememberNavController()
            BabbabTheme {
                Scaffold(bottomBar = { BottomNavigationBar(navController) }) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding),
                    ) {
                        composable(Screen.Home.route, content = { Home() })
                        composable(
                            Screen.FriendsList.route,
                            content = { BorderlessTextButton(text = "Friend List") {} })
                        composable(
                            Screen.Settings.route,
                            content = { BorderlessTextButton(text = "Settings") {} })
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (Firebase.auth.currentUser == null) {
            startActivity(LoginActivity.createIntent(this))
            finish()
            return
        }
        if (checkSelfPermissionCompat(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            requestPermissionsCompat(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_READ_STORAGE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_READ_STORAGE -> if (grantResults.any { it == PackageManager.PERMISSION_DENIED }) {
                Toast.makeText(this, "R.string.read_storage_permission_denied", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    companion object {
        const val PERMISSION_REQUEST_READ_STORAGE = 0
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    BottomNavigation {
        Screen.items.forEach { screen ->
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            BottomNavigationItem(
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                    Timber.d(navController.currentDestination?.route)
                    Timber.d(screen.route)
                },
                icon = { Icon(screen.icon, contentDescription = screen.route) },
                label = { Text(stringResource(screen.name)) },
            )
        }
    }
}

@Preview(name = "Light Theme")
@Preview(
    name = "Dark Theme",
    uiMode = UI_MODE_NIGHT_YES,
)
@Composable
fun MainActivityPreview() {
    val navController = rememberNavController()
    navController.navigate(Screen.items.first().route)
    BabbabTheme {
        Scaffold(bottomBar = { BottomNavigationBar(navController) }) {
        }
    }
}
