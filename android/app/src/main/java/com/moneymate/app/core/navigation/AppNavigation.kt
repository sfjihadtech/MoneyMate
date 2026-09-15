package com.moneymate.app.core.navigation

// =============================================================================
// File: AppNavigation.kt
// Purpose: Defines the application navigation graph and connects authentication, onboarding, and main app destinations.
// Notes: Major sections below are commented so the code is easier to read,
//        maintain, and safely extend without changing existing behavior.
// =============================================================================

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.moneymate.app.data.repository.MoneyMateRepository
import com.moneymate.app.feature.auth.ui.*
import com.moneymate.app.feature.main.ui.MoneyMateMainApp
import com.moneymate.app.feature.onboarding.ui.SplashScreen


// -----------------------------------------------------------------------------
// Section: AppRoutes
// Purpose: Navigation/routing logic for App Routes.
// -----------------------------------------------------------------------------
object AppRoutes {
    const val SPLASH = "splash"
    const val WELCOME = "welcome"
    const val SIGN_IN = "sign_in"
    const val CREATE_ACCOUNT = "create_account"
    const val FORGOT_PASSWORD = "forgot_password"
    const val RESET_PASSWORD = "reset_password?token={token}"
    const val PASSWORD_RESET_SUCCESS = "password_reset_success"
    const val HOME = "home"
}


// -----------------------------------------------------------------------------
// Section: AppNavigation
// Purpose: Navigation/routing logic for App Navigation.
// -----------------------------------------------------------------------------
@Composable
fun AppNavigation(onDarkModeChange: (Boolean) -> Unit) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val repo = MoneyMateRepository(context.applicationContext)

    fun enterHome() {
        navController.navigate(AppRoutes.HOME) {
            popUpTo(AppRoutes.WELCOME) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun backToWelcome() {
        repo.logout()
        navController.navigate(AppRoutes.WELCOME) {
            popUpTo(0)
            launchSingleTop = true
        }
    }

    NavHost(navController = navController, startDestination = AppRoutes.SPLASH) {
        composable(AppRoutes.SPLASH) {
            SplashScreen {
                val next = if (repo.hasSession()) AppRoutes.HOME else AppRoutes.WELCOME
                navController.navigate(next) { popUpTo(AppRoutes.SPLASH) { inclusive = true } }
            }
        }

        composable(AppRoutes.WELCOME) {
            WelcomeScreen(
                onSignIn = { navController.navigate(AppRoutes.SIGN_IN) },
                onCreateAccount = { navController.navigate(AppRoutes.CREATE_ACCOUNT) }
            )
        }

        composable(AppRoutes.SIGN_IN) {
            SignInScreen(
                onBack = { navController.popBackStack() },
                onSignedIn = { enterHome() },
                onCreateAccount = { navController.navigate(AppRoutes.CREATE_ACCOUNT) },
                onForgotPassword = { navController.navigate(AppRoutes.FORGOT_PASSWORD) }
            )
        }

        composable(AppRoutes.CREATE_ACCOUNT) {
            CreateAccountScreen(
                onBack = { navController.popBackStack() },
                onCreated = { enterHome() },
                onSignIn = { navController.navigate(AppRoutes.SIGN_IN) }
            )
        }

        composable(AppRoutes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() },
                onResetTokenReady = { _ ->
                    // Do not navigate automatically.
                    // The Reset Password screen must open only when the user
                    // taps the secure reset link received by email.
                }
            )
        }

        composable(
            route = AppRoutes.RESET_PASSWORD,
            arguments = listOf(navArgument("token") { type = NavType.StringType; defaultValue = "" }),
            deepLinks = listOf(navDeepLink { uriPattern = "moneymate://reset-password?token={token}" })
        ) { entry ->
            ResetPasswordScreen(
                token = entry.arguments?.getString("token").orEmpty(),
                onBack = { navController.popBackStack() },
                onSuccess = { navController.navigate(AppRoutes.PASSWORD_RESET_SUCCESS) }
            )
        }

        composable(AppRoutes.PASSWORD_RESET_SUCCESS) {
            PasswordResetSuccessScreen {
                navController.navigate(AppRoutes.SIGN_IN) {
                    popUpTo(AppRoutes.WELCOME)
                    launchSingleTop = true
                }
            }
        }

        composable(AppRoutes.HOME) {
            MoneyMateMainApp(
                onSignOut = { backToWelcome() },
                onDarkModeChange = onDarkModeChange,
                guestMode = false
            )
        }

    }
}
