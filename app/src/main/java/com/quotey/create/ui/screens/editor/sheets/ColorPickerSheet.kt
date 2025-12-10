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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
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
            SheetHeader(title = "Custom Color", onDismiss = onDismiss)

            Spacer(modifier = Modifier.height(16.dp))

            // Saturation-Value picker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                SaturationValuePicker(
                    hue = hue,
                    saturation = saturation,
                    value = value,
                    onSaturationValueChanged = { s, v ->
                        saturation = s
                        value = v
                        updateHexInput(hue, s, v) { hexInput = it }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

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

            Spacer(modifier = Modifier.height(20.dp))

            // Alpha slider
            SectionTitle("Opacity")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AlphaSlider(
                    alpha = alpha,
                    color = currentColor.copy(alpha = 1f),
                    onAlphaChanged = { alpha = it },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "${(alpha * 100).roundToInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Preview and hex input row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color preview
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Preview",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(4.dp, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(checkerboardBrush())
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(currentColor)
                        )
                    }
                }

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
                    label = { Text("Hex Color") },
                    prefix = { Text("#") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // RGB values display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RgbValueChip(
                    "R",
                    (currentColor.red * 255).roundToInt(),
                    Color.Red.copy(alpha = 0.15f),
                    Modifier.weight(1f)
                )
                RgbValueChip(
                    "G",
                    (currentColor.green * 255).roundToInt(),
                    Color.Green.copy(alpha = 0.15f),
                    Modifier.weight(1f)
                )
                RgbValueChip(
                    "B",
                    (currentColor.blue * 255).roundToInt(),
                    Color.Blue.copy(alpha = 0.15f),
                    Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Apply button
            Button(
                onClick = {
                    onColorSelected(currentColor.value.toLong())
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Apply Color",
                    style = MaterialTheme.typography.labelLarge
                )
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
            .aspectRatio(1f)
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

            // Draw picker indicator
            val indicatorX = saturation * size.width
            val indicatorY = (1f - value) * size.height
            val indicatorRadius = 12.dp.toPx()

            // Outer ring (white)
            drawCircle(
                color = Color.White,
                radius = indicatorRadius,
                center = Offset(indicatorX, indicatorY),
                style = Stroke(width = 3.dp.toPx())
            )

            // Inner ring (black for contrast)
            drawCircle(
                color = Color.Black.copy(alpha = 0.3f),
                radius = indicatorRadius - 2.dp.toPx(),
                center = Offset(indicatorX, indicatorY),
                style = Stroke(width = 1.dp.toPx())
            )

            // Filled center with current color
            drawCircle(
                color = Color.hsv(hue, saturation, value),
                radius = indicatorRadius - 4.dp.toPx(),
                center = Offset(indicatorX, indicatorY)
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
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
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

            // Draw indicator
            val indicatorX = (hue / 360f) * size.width
            val indicatorWidth = 8.dp.toPx()

            drawRoundRect(
                color = Color.White,
                topLeft = Offset(indicatorX - indicatorWidth / 2, 0f),
                size = Size(indicatorWidth, size.height.toFloat()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
                style = Stroke(width = 3.dp.toPx())
            )

            drawRoundRect(
                color = Color.Black.copy(alpha = 0.2f),
                topLeft = Offset(indicatorX - indicatorWidth / 2 + 1.5f.dp.toPx(), 1.5f.dp.toPx()),
                size = Size(indicatorWidth - 3.dp.toPx(), size.height.toFloat() - 3.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.5f.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}

@Composable
private fun AlphaSlider(
    alpha: Float,
    color: Color,
    onAlphaChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var size by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(checkerboardBrush())
            .onSizeChanged { size = it }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onAlphaChanged((offset.x / size.width).coerceIn(0f, 1f))
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    onAlphaChanged((change.position.x / size.width).coerceIn(0f, 1f))
                }
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            // Draw alpha gradient
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(color.copy(alpha = 0f), color)
                )
            )

            // Draw indicator
            val indicatorX = alpha * size.width
            val indicatorWidth = 8.dp.toPx()

            drawRoundRect(
                color = Color.White,
                topLeft = Offset(indicatorX - indicatorWidth / 2, 0f),
                size = Size(indicatorWidth, size.height.toFloat()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
                style = Stroke(width = 3.dp.toPx())
            )

            drawRoundRect(
                color = Color.Black.copy(alpha = 0.2f),
                topLeft = Offset(indicatorX - indicatorWidth / 2 + 1.5f.dp.toPx(), 1.5f.dp.toPx()),
                size = Size(indicatorWidth - 3.dp.toPx(), size.height.toFloat() - 3.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.5f.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}

@Composable
private fun RgbValueChip(
    label: String,
    value: Int,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun checkerboardBrush(): Brush {
    return Brush.linearGradient(
        colors = listOf(Color.LightGray.copy(alpha = 0.3f), Color.White.copy(alpha = 0.3f))
    )
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
