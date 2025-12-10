package com.quotey.create

import android.app.Activity
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontMonospace
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quotey.create.ui.theme.QuoteyTheme

class DebugActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_ERROR_MESSAGE = "error_message"
        const val EXTRA_ERROR_STACKTRACE = "error_stacktrace"
        const val EXTRA_ERROR_CAUSE = "error_cause"
    }

    private var errorMessage: String = ""
    private var errorStacktrace: String = ""
    private var errorCause: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        errorMessage = intent?.getStringExtra(EXTRA_ERROR_MESSAGE) ?: "Unknown Error"
        errorStacktrace = intent?.getStringExtra(EXTRA_ERROR_STACKTRACE) ?: ""
        errorCause = intent?.getStringExtra(EXTRA_ERROR_CAUSE) ?: ""

        setContent {
            QuoteyTheme {
                DebugErrorScreen(
                    errorMessage = errorMessage,
                    errorStacktrace = errorStacktrace,
                    errorCause = errorCause,
                    onCopy = { copyToClipboard() },
                    onRestart = { restartApp() }
                )
            }
        }
    }

    private fun copyToClipboard() {
        val fullError = buildString {
            appendLine("=== QUOTEY DEBUG ERROR REPORT ===")
            appendLine("Timestamp: ${System.currentTimeMillis()}")
            appendLine("Device: ${Build.DEVICE}")
            appendLine("Model: ${Build.MODEL}")
            appendLine("Android Version: ${Build.VERSION.RELEASE}")
            appendLine()
            appendLine("ERROR MESSAGE:")
            appendLine(errorMessage)
            appendLine()
            if (errorCause.isNotEmpty()) {
                appendLine("CAUSE:")
                appendLine(errorCause)
                appendLine()
            }
            appendLine("STACKTRACE:")
            appendLine(errorStacktrace)
            appendLine()
            appendLine("=== END REPORT ===")
        }

        val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = android.content.ClipData.newPlainText("error_report", fullError)
        clipboard?.setPrimaryClip(clip)

        // Show toast
        android.widget.Toast.makeText(this, "Error report copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun restartApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}

@Composable
private fun DebugErrorScreen(
    errorMessage: String,
    errorStacktrace: String,
    errorCause: String,
    onCopy: () -> Unit,
    onRestart: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = "⚠️ App Crashed",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Text(
                    text = "An unexpected error occurred. Please review the details below.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            // Error Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                // Error Message Section
                Text(
                    text = "Error Message",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            fontFamily = FontMonospace
                        ),
                        modifier = Modifier.padding(12.dp),
                        maxLines = 5
                    )
                }

                // Cause Section
                if (errorCause.isNotEmpty()) {
                    Text(
                        text = "Cause",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = errorCause,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp,
                                fontFamily = FontMonospace
                            ),
                            modifier = Modifier.padding(12.dp),
                            maxLines = 3
                        )
                    }
                }

                // Stacktrace Section
                Text(
                    text = "Stack Trace",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (errorStacktrace.isEmpty()) "No stack trace available" else errorStacktrace,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 10.sp,
                            fontFamily = FontMonospace
                        ),
                        modifier = Modifier.padding(12.dp),
                        maxLines = 10
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onCopy,
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Copy")
                }

                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Restart")
                }
            }
        }
    }
}
