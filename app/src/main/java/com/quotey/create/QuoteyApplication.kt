package com.quotey.create

import android.app.Application
import android.content.Intent
import android.os.Process
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class QuoteyApplication : Application() {

    private var defaultHandler: Thread.UncaughtExceptionHandler? = null

    override fun onCreate() {
        super.onCreate()

        // Store the default handler
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        // Set up global exception handler EARLY in the application lifecycle
        // This catches crashes before any activity is created
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            handleUncaughtException(thread, throwable)
        }

        Log.d(TAG, "QuoteyApplication initialized with crash handler")
    }

    private fun handleUncaughtException(thread: Thread, throwable: Throwable) {
        try {
            Log.e(TAG, "Uncaught exception on thread ${thread.name}", throwable)

            val errorMessage = throwable.message ?: throwable.javaClass.simpleName
            val stackTrace = throwable.stackTraceToString()
            val cause = throwable.cause?.toString() ?: ""

            val intent = Intent(this, DebugActivity::class.java).apply {
                putExtra(DebugActivity.EXTRA_ERROR_MESSAGE, errorMessage)
                putExtra(DebugActivity.EXTRA_ERROR_STACKTRACE, stackTrace)
                putExtra(DebugActivity.EXTRA_ERROR_CAUSE, cause)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                )
            }
            startActivity(intent)

            // Give the intent time to launch
            Thread.sleep(500)

        } catch (e: Exception) {
            Log.e(TAG, "Error launching DebugActivity", e)
            // Fall back to default handler if we can't launch debug activity
            defaultHandler?.uncaughtException(thread, throwable)
        } finally {
            // Kill the process to ensure clean state
            Process.killProcess(Process.myPid())
            System.exit(1)
        }
    }

    companion object {
        private const val TAG = "QuoteyApplication"
    }
}
