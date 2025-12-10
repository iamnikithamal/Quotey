package com.quotey.create.ui.screens.editor.sheets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun ColorPickerSheet(
    initialColor: Long,
    onDismiss: () -> Unit,
    onColorSelected: (Long) -> Unit
) {
    val initialColorObj = Color(initialColor.toULong())

    // Convert initial color to HSV
    val initialHsv = remember(initialColor) {
        val r = initialColorObj.red
        val g = initialColorObj.green
        val b = initialColorObj.blue
        rgbToHsv(r, g, b)
    }

    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var value by remember { mutableFloatStateOf(initialHsv[2]) }
    var alpha by remember { mutableFloatStateOf(initialColorObj.alpha) }

    var hexInput by remember(initialColor) {
        mutableStateOf(
            String.format(
                "%02X%02X%02X",
                (initialColorObj.red * 255).roundToInt(),
                (initialColorObj.green * 255).roundToInt(),
                (initialColorObj.blue * 255).roundToInt()
            )
        )
    }

    val currentColor = remember(hue, saturation, value, alpha) {
        Color.hsv(hue, saturation, value, alpha)
    }

    BottomSheetContainer(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            SheetHeader(title = "Choose Color", onDismiss = onDismiss)

            Spacer(modifier = Modifier.height(16.dp))

            // Color preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Current color preview
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(currentColor)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(12.dp)
                        )
                )

                // Hex input
                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { newValue ->
                        if (newValue.length <= 6 && newValue.all { it.isDigit() || it in 'A'..'F' || it in 'a'..'f' }) {
                            hexInput = newValue.uppercase()
                            if (newValue.length == 6) {
                                try {
                                    val color = android.graphics.Color.parseColor("#$newValue")
                                    val r = android.graphics.Color.red(color) / 255f
                                    val g = android.graphics.Color.green(color) / 255f
                                    val b = android.graphics.Color.blue(color) / 255f
                                    val hsv = rgbToHsv(r, g, b)
                                    hue = hsv[0]
                                    saturation = hsv[1]
                                    value = hsv[2]
                                } catch (e: Exception) {
                                    // Invalid hex
                                }
                            }
                        }
                    },
                    label = { Text("Hex") },
                    prefix = { Text("#") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Saturation-Value picker
            SectionTitle("Color")
            SaturationValuePicker(
                hue = hue,
                saturation = saturation,
                value = value,
                onSaturationValueChanged = { s, v ->
                    saturation = s
                    value = v
                    updateHexInput(hue, s, v) { hexInput = it }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Hue slider
            SectionTitle("Hue")
            HueSlider(
                hue = hue,
                onHueChanged = {
                    hue = it
                    updateHexInput(it, saturation, value) { hex -> hexInput = hex }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Alpha slider
            SectionTitle("Opacity")
            Slider(
                value = alpha,
                onValueChange = { alpha = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = SliderDefaults.colors(
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            )
            Text(
                text = "${(alpha * 100).roundToInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Apply button
            Button(
                onClick = {
                    onColorSelected(currentColor.value.toLong())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Apply Color")
            }
        }
    }
}

@Composable
private fun SaturationValuePicker(
    hue: Float,
    saturation: Float,
    value: Float,
    onSaturationValueChanged: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var size by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .onSizeChanged { size = it }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val s = (offset.x / size.width).coerceIn(0f, 1f)
                    val v = 1f - (offset.y / size.height).coerceIn(0f, 1f)
                    onSaturationValueChanged(s, v)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val s = (change.position.x / size.width).coerceIn(0f, 1f)
                    val v = 1f - (change.position.y / size.height).coerceIn(0f, 1f)
                    onSaturationValueChanged(s, v)
                }
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            // Draw saturation gradient (white to hue color)
            val hueColor = Color.hsv(hue, 1f, 1f)
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.White, hueColor)
                )
            )
            // Draw value gradient (transparent to black)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black)
                )
            )

            // Draw selector circle
            val selectorX = saturation * size.width
            val selectorY = (1f - value) * size.height
            drawCircle(
                color = Color.White,
                radius = 12.dp.toPx(),
                center = Offset(selectorX, selectorY),
                style = Stroke(width = 3.dp.toPx())
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.3f),
                radius = 12.dp.toPx(),
                center = Offset(selectorX, selectorY),
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}

@Composable
private fun HueSlider(
    hue: Float,
    onHueChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var size by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .onSizeChanged { size = it }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newHue = (offset.x / size.width * 360f).coerceIn(0f, 360f)
                    onHueChanged(newHue)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val newHue = (change.position.x / size.width * 360f).coerceIn(0f, 360f)
                    onHueChanged(newHue)
                }
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            // Draw rainbow gradient
            val hueColors = (0..360 step 30).map { Color.hsv(it.toFloat(), 1f, 1f) }
            drawRect(
                brush = Brush.horizontalGradient(hueColors)
            )

            // Draw selector
            val selectorX = (hue / 360f) * size.width
            drawCircle(
                color = Color.White,
                radius = 16.dp.toPx(),
                center = Offset(selectorX, size.height / 2f),
                style = Stroke(width = 3.dp.toPx())
            )
            drawCircle(
                color = Color.hsv(hue, 1f, 1f),
                radius = 12.dp.toPx(),
                center = Offset(selectorX, size.height / 2f)
            )
        }
    }
}

private fun rgbToHsv(r: Float, g: Float, b: Float): FloatArray {
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min

    val h = when {
        delta == 0f -> 0f
        max == r -> 60f * (((g - b) / delta) % 6)
        max == g -> 60f * (((b - r) / delta) + 2)
        else -> 60f * (((r - g) / delta) + 4)
    }.let { if (it < 0) it + 360 else it }

    val s = if (max == 0f) 0f else delta / max
    val v = max

    return floatArrayOf(h, s, v)
}

private fun updateHexInput(hue: Float, saturation: Float, value: Float, onUpdate: (String) -> Unit) {
    val color = Color.hsv(hue, saturation, value)
    onUpdate(
        String.format(
            "%02X%02X%02X",
            (color.red * 255).roundToInt(),
            (color.green * 255).roundToInt(),
            (color.blue * 255).roundToInt()
        )
    )
}
