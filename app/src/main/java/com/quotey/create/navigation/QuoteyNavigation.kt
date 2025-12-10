package com.quotey.create.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.quotey.create.ui.screens.editor.EditorScreen
import com.quotey.create.ui.screens.home.HomeScreen
import com.quotey.create.ui.screens.onboarding.OnboardingScreen

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object Editor : Screen("editor")
}

@Composable
fun QuoteyNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String,
    modifier: Modifier = Modifier
) {
    val animationDuration = 400

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(animationDuration, easing = EaseInOut)) +
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(animationDuration, easing = EaseInOut),
                        initialOffset = { it / 4 }
                    )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(animationDuration, easing = EaseInOut)) +
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(animationDuration, easing = EaseInOut),
                        targetOffset = { it / 4 }
                    )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(animationDuration, easing = EaseInOut)) +
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(animationDuration, easing = EaseInOut),
                        initialOffset = { it / 4 }
                    )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(animationDuration, easing = EaseInOut)) +
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(animationDuration, easing = EaseInOut),
                        targetOffset = { it / 4 }
                    )
        }
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToEditor = {
                    navController.navigate(Screen.Editor.route)
                }
            )
        }

        composable(Screen.Editor.route) {
            EditorScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
