package com.quotey.create

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.quotey.create.data.model.ThemeMode
import com.quotey.create.data.preferences.PreferencesManager
import com.quotey.create.navigation.QuoteyNavHost
import com.quotey.create.navigation.Screen
import com.quotey.create.ui.theme.QuoteyTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val hasCompletedOnboarding by preferencesManager.hasCompletedOnboarding.collectAsState(initial = null)
            val themeMode by preferencesManager.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

            // Keep splash screen while loading preferences
            splashScreen.setKeepOnScreenCondition {
                hasCompletedOnboarding == null
            }

            hasCompletedOnboarding?.let { completed ->
                QuoteyApp(
                    hasCompletedOnboarding = completed,
                    themeMode = themeMode
                )
            }
        }
    }
}

@Composable
fun QuoteyApp(
    hasCompletedOnboarding: Boolean,
    themeMode: ThemeMode
) {
    val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    QuoteyTheme(darkTheme = isDarkTheme) {
        val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(systemBarsPadding)
        ) {
            val navController = rememberNavController()
            val startDestination = if (hasCompletedOnboarding) {
                Screen.Home.route
            } else {
                Screen.Onboarding.route
            }

            QuoteyNavHost(
                navController = navController,
                startDestination = startDestination
            )
        }
    }
}
