package com.example.mindcare.navigation

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import com.example.mindcare.ui.screens.auth.ForgotPasswordScreen
import com.example.mindcare.ui.screens.auth.LoginScreen
import com.example.mindcare.ui.screens.auth.SignUpScreen
import com.example.mindcare.ui.screens.chat.ChatScreen
import com.example.mindcare.ui.screens.dashboard.DashboardScreen
import com.example.mindcare.ui.screens.settings.SettingsScreen

// Easing smooth seperti material 3
private val EaseOutQuart = CubicBezierEasing(0.25f, 1f, 0.5f, 1f)
private val EaseInQuart = CubicBezierEasing(0.5f, 0f, 0.75f, 0f)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        // Maju: layar baru zoom in + fade in
        enterTransition = {
            scaleIn(tween(360, easing = EaseOutQuart), initialScale = 0.94f) +
                fadeIn(tween(360, easing = EaseOutQuart))
        },
        // Maju: layar lama fade out saja (tidak bergerak)
        exitTransition = {
            fadeOut(tween(200, easing = EaseInQuart))
        },
        // Balik: layar di belakang muncul kembali dengan fade
        popEnterTransition = {
            fadeIn(tween(300, easing = EaseOutQuart))
        },
        // Balik: layar atas zoom out + fade out
        popExitTransition = {
            scaleOut(tween(300, easing = EaseInQuart), targetScale = 0.94f) +
                fadeOut(tween(300, easing = EaseInQuart))
        }
    ) {
        composable(Routes.LOGIN) { LoginScreen(navController) }
        composable(Routes.SIGN_UP) { SignUpScreen(navController) }
        composable(Routes.FORGOT_PASSWORD) { ForgotPasswordScreen(navController) }
        composable(Routes.DASHBOARD) { DashboardScreen(navController) }
        composable(Routes.SETTINGS) { SettingsScreen(navController) }
        composable(
            route = "chat?sessionId={sessionId}",
            arguments = listOf(navArgument("sessionId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            ChatScreen(
                navController = navController,
                initialSessionId = backStackEntry.arguments?.read { getStringOrNull("sessionId") }
            )
        }
    }
}
