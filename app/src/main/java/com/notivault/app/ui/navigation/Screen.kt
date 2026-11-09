package com.notivault.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Inbox : Screen("inbox", "Inbox", Icons.Default.Inbox)
    object Rules : Screen("rules", "App Rules", Icons.Default.Security)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Onboarding : Screen("onboarding", "Permissions Setup")
    object Lock : Screen("lock", "Unlock Vault")
}
