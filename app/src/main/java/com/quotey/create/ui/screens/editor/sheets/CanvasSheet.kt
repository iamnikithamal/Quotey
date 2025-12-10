package com.quotey.create.ui.screens.editor.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quotey.create.data.model.AspectRatio
import com.quotey.create.data.model.CanvasSettings

@Composable
fun CanvasSheet(
    canvas: CanvasSettings,
    onDismiss: () -> Unit,
    onAspectRatioChanged: (AspectRatio) -> Unit,
    onCustomSizeChanged: (Int, Int) -> Unit,
    onCornerRadiusChanged: (Float) -> Unit,
    onPaddingChanged: (Float) -> Unit
) {
    var customWidth by remember(canvas.customWidth) { mutableStateOf(canvas.customWidth.toString()) }
    var customHeight by remember(canvas.customHeight) { mutableStateOf(canvas.customHeight.toString()) }

    BottomSheetContainer(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            SheetHeader(title = "Canvas Settings", onDismiss = onDismiss)

            Spacer(modifier = Modifier.height(16.dp))

            // Aspect ratio selection
            SectionTitle("Aspect Ratio")

            // Common presets
            val socialPresets = listOf(
                AspectRatio.SQUARE,
                AspectRatio.PORTRAIT,
                AspectRatio.STORY,
                AspectRatio.LANDSCAPE
            )

            val platformPresets = listOf(
                AspectRatio.TWITTER,
                AspectRatio.FACEBOOK,
                AspectRatio.PINTEREST,
                AspectRatio.LINKEDIN,
                AspectRatio.YOUTUBE_THUMB
            )

            Text(
                text = "Common Sizes",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                socialPresets.forEach { ratio ->
                    AspectRatioCard(
                        aspectRatio = ratio,
                        isSelected = canvas.aspectRatio == ratio,
                        onClick = { onAspectRatioChanged(ratio) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Platform Specific",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                platformPresets.take(3).forEach { ratio ->
                    AspectRatioCard(
                        aspectRatio = ratio,
                        isSelected = canvas.aspectRatio == ratio,
                        onClick = { onAspectRatioChanged(ratio) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                platformPresets.drop(3).forEach { ratio ->
                    AspectRatioCard(
                        aspectRatio = ratio,
                        isSelected = canvas.aspectRatio == ratio,
                        onClick = { onAspectRatioChanged(ratio) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Custom option
                AspectRatioCard(
                    aspectRatio = AspectRatio.CUSTOM,
                    isSelected = canvas.aspectRatio == AspectRatio.CUSTOM,
                    onClick = { onAspectRatioChanged(AspectRatio.CUSTOM) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Custom size inputs
            if (canvas.aspectRatio == AspectRatio.CUSTOM) {
                Spacer(modifier = Modifier.height(24.dp))

                SectionTitle("Custom Size (pixels)")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = customWidth,
                        onValueChange = {
                            customWidth = it
                            it.toIntOrNull()?.let { w ->
                                onCustomSizeChanged(w, canvas.customHeight)
                            }
                        },
                        label = { Text("Width") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = customHeight,
                        onValueChange = {
                            customHeight = it
                            it.toIntOrNull()?.let { h ->
                                onCustomSizeChanged(canvas.customWidth, h)
                            }
                        },
                        label = { Text("Height") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // Quick size buttons
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "1080x1080" to Pair(1080, 1080),
                        "1080x1350" to Pair(1080, 1350),
                        "1080x1920" to Pair(1080, 1920),
                        "1920x1080" to Pair(1920, 1080)
                    ).forEach { (label, size) ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    customWidth = size.first.toString()
                                    customHeight = size.second.toString()
                                    onCustomSizeChanged(size.first, size.second)
                                },
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Current size display
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Width",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${canvas.width}px",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Height",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${canvas.height}px",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Ratio",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${canvas.aspectRatio.ratioWidth}:${canvas.aspectRatio.ratioHeight}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Corner radius
            SectionTitle("Corner Radius")
            SliderWithValue(
                value = canvas.cornerRadius,
                onValueChange = onCornerRadiusChanged,
                valueRange = 0f..100f,
                valueLabel = "${canvas.cornerRadius.toInt()} dp",
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Quick corner radius presets
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0f, 8f, 16f, 24f, 32f, 48f).forEach { radius ->
                    val isSelected = canvas.cornerRadius == radius
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onCornerRadiusChanged(radius) },
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = radius.toInt().toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Padding
            SectionTitle("Content Padding")
            SliderWithValue(
                value = canvas.padding,
                onValueChange = onPaddingChanged,
                valueRange = 0f..100f,
                valueLabel = "${canvas.padding.toInt()} dp",
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Quick padding presets
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0f, 16f, 24f, 32f, 48f, 64f).forEach { padding ->
                    val isSelected = canvas.padding == padding
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onPaddingChanged(padding) },
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = padding.toInt().toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AspectRatioCard(
    aspectRatio: AspectRatio,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Mini preview
            val previewRatio = aspectRatio.ratioWidth.toFloat() / aspectRatio.ratioHeight.toFloat()
            val (boxWidth, boxHeight) = if (previewRatio > 1) {
                32.dp to (32.dp / previewRatio)
            } else {
                (32.dp * previewRatio) to 32.dp
            }

            Box(
                modifier = Modifier
                    .width(boxWidth)
                    .height(boxHeight)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isSelected)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(4.dp)
                    )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = aspectRatio.displayName.substringBefore(" ("),
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
