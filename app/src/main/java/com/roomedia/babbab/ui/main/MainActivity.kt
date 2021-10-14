package com.roomedia.babbab.ui.main

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.net.Uri
import android.os.Bundle
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
import com.roomedia.babbab.ui.main.screen.Friends
import com.roomedia.babbab.ui.main.screen.Home
import com.roomedia.babbab.ui.main.screen.Screen
import com.roomedia.babbab.ui.main.screen.Settings
import com.roomedia.babbab.ui.theme.BabbabTheme
import java.util.*

class MainActivity : AppCompatActivity(), Home, Friends, Settings, ActivityCompat.OnRequestPermissionsResultCallback {

    override val latestTmpUri by lazy { getTmpUri() }
    override val targetUri: MutableState<Uri?> = mutableStateOf(null)

    override val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { setPhotoUri() }
    override val selectPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent(), ::setPhotoUri)

    override val settingsScreenTime = mutableStateOf(0L)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Firebase.auth.currentUser == null) {
            startActivity(LoginActivity.createIntent(this))
            finish()
            return
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
                        composable(Screen.Friends.route, content = { Friends() })
                        composable(Screen.Settings.route, content = {
                            settingsScreenTime.value = Date().time
                            Settings()
                        })
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
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestReadStoragePermissionsResult(requestCode, grantResults)
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
    BabbabTheme {
        Scaffold(bottomBar = { BottomNavigationBar(navController) }) {
        }
    }
}
