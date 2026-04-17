package com.sergivm.monsterpacks.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.sergivm.monsterpacks.R
import com.sergivm.monsterpacks.presentation.screen.collection.CollectionScreen
import com.sergivm.monsterpacks.presentation.screen.main.MainScreen
import com.sergivm.monsterpacks.presentation.screen.settings.SettingsScreen
import com.sergivm.monsterpacks.presentation.screen.shop.ShopScreen
import com.sergivm.monsterpacks.presentation.ui.theme.MonsterPacksTheme
import com.sergivm.monsterpacks.presentation.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MonsterPacksTheme {
                MonsterPacksNavHost(onExit = { finish() })
            }
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: String) {
    object Pack       : Screen("pack",       "Pack",       "🎴")
    object Collection : Screen("collection", "Collection", "📖")
    object Shop       : Screen("shop",       "Shop",       "🛒")
    object Settings   : Screen("settings",   "Settings",   "⚙")
}

private val bottomNavItems = listOf(
    Screen.Pack,
    Screen.Collection,
    Screen.Shop,
    Screen.Settings
)

@Composable
fun MonsterPacksNavHost(
    mainViewModel: MainViewModel = hiltViewModel(),
    onExit: () -> Unit
) {
    val navController = rememberNavController()
    val mainState by mainViewModel.uiState.collectAsState()

    var isOpeningPack by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    // Handle Back Button
    BackHandler {
        if (showExitDialog) {
            showExitDialog = false
        } else {
            showExitDialog = true
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(stringResource(R.string.exit_dialog_title)) },
            text = { Text(stringResource(R.string.exit_dialog_message)) },
            confirmButton = {
                TextButton(onClick = onExit) {
                    Text(stringResource(R.string.exit_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(stringResource(R.string.exit_dialog_cancel))
                }
            }
        )
    }

    // Hide bottom bar if username is missing or a pack is opening
    val showBottomBar = !mainState.isFirstLaunch && !mainState.isLoading && !isOpeningPack

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Text(screen.icon, fontSize = 20.sp) },
                            label = { Text(screen.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Pack.route,
            modifier = Modifier.padding(if (showBottomBar) innerPadding else PaddingValues(0.dp))
        ) {
            composable(Screen.Pack.route) {
                MainScreen(
                    viewModel = mainViewModel,
                    onPackOpeningStarted  = { isOpeningPack = true },
                    onPackOpeningFinished = { isOpeningPack = false }
                )
            }
            composable(Screen.Collection.route) { CollectionScreen() }
            composable(Screen.Shop.route) { 
                ShopScreen(
                    onNavigateToPacks = {
                        navController.navigate(Screen.Pack.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                ) 
            }
            composable(Screen.Settings.route)   { SettingsScreen() }
        }
    }
}
