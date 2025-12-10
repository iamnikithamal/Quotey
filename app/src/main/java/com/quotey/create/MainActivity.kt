package com.quotey.create

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
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

        // Set up global exception handler
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            handleUncaughtException(thread, throwable)
        }

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

    private fun handleUncaughtException(thread: Thread, throwable: Throwable) {
        val errorMessage = throwable.message ?: throwable.javaClass.simpleName
        val stackTrace = throwable.stackTraceToString()
        val cause = throwable.cause?.toString() ?: ""

        val intent = Intent(this, DebugActivity::class.java).apply {
            putExtra(DebugActivity.EXTRA_ERROR_MESSAGE, errorMessage)
            putExtra(DebugActivity.EXTRA_ERROR_STACKTRACE, stackTrace)
            putExtra(DebugActivity.EXTRA_ERROR_CAUSE, cause)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        finish()
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
        Surface(
            modifier = Modifier.fillMaxSize()
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
