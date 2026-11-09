package com.notivault.app.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.notivault.app.ui.navigation.Screen
import com.notivault.app.ui.screens.inbox.InboxScreen
import com.notivault.app.ui.screens.inbox.InboxViewModel
import com.notivault.app.ui.screens.onboarding.OnboardingScreen
import com.notivault.app.ui.screens.rules.RulesScreen
import com.notivault.app.ui.screens.rules.RulesViewModel
import com.notivault.app.ui.screens.settings.SettingsScreen
import com.notivault.app.ui.screens.settings.SettingsViewModel
import com.notivault.app.ui.theme.NotiVaultTheme

class MainActivity : AppCompatActivity() {

    private val inboxViewModel: InboxViewModel by viewModels()
    private val rulesViewModel: RulesViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NotiVaultTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val bottomBarScreens = listOf(
                    Screen.Inbox,
                    Screen.Rules,
                    Screen.Settings
                )

                val showBottomBar = currentRoute in bottomBarScreens.map { it.route }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                bottomBarScreens.forEach { screen ->
                                    NavigationBarItem(
                                        icon = { screen.icon?.let { Icon(it, contentDescription = screen.title) } },
                                        label = { Text(screen.title) },
                                        selected = currentRoute == screen.route,
                                        onClick = {
                                            if (currentRoute != screen.route) {
                                                navController.navigate(screen.route) {
                                                    popUpTo(Screen.Inbox.route) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Inbox.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Inbox.route) {
                            InboxScreen(viewModel = inboxViewModel)
                        }
                        composable(Screen.Rules.route) {
                            RulesScreen(viewModel = rulesViewModel)
                        }
                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                viewModel = settingsViewModel,
                                onNavigateToOnboarding = { navController.navigate(Screen.Onboarding.route) }
                            )
                        }
                        composable(Screen.Onboarding.route) {
                            OnboardingScreen(
                                onFinish = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
