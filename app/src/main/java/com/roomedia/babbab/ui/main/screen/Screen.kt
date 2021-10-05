package com.roomedia.babbab.ui.main.screen

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.roomedia.babbab.R

sealed class Screen(
    val route: String,
    val icon: ImageVector,
    @StringRes val name: Int,
) {
    object Home : Screen("home", Icons.Default.Home, R.string.home)
    object Friends : Screen("friends", Icons.Default.Search, R.string.friends)
    object Settings : Screen("settings", Icons.Default.Settings, R.string.setting)

    companion object {
        val items = listOf(
            Home,
            Friends,
            Settings,
        )
    }
}
