package com.sergivm.monsterpacks.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.sergivm.monsterpacks.presentation.screen.collection.CollectionScreen
import com.sergivm.monsterpacks.presentation.screen.main.MainScreen
import com.sergivm.monsterpacks.presentation.screen.settings.SettingsScreen
import com.sergivm.monsterpacks.presentation.screen.shop.ShopScreen
import com.sergivm.monsterpacks.presentation.ui.theme.MonsterPacksTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MonsterPacksTheme {
                MonsterPacksNavHost()
            }
        }
    }
}

// ── Navigation destinations ───────────────────────────────────────────────────

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

// ── Nav host ──────────────────────────────────────────────────────────────────

@Composable
fun MonsterPacksNavHost() {
    val navController = rememberNavController()

    // Hide bottom bar while a pack is being opened
    var isOpeningPack by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            if (!isOpeningPack) {
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
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Pack.route) {
                MainScreen(
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
