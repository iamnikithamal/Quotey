package com.quotey.create.ui.screens.editor.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Gradient
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Pattern
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quotey.create.data.model.BackgroundSettings
import com.quotey.create.data.model.BackgroundType
import com.quotey.create.data.model.GradientSettings
import com.quotey.create.data.model.GradientType
import com.quotey.create.data.model.PatternSettings
import com.quotey.create.data.model.PatternType
import com.quotey.create.ui.screens.editor.ColorPickerTarget
import com.quotey.create.ui.theme.PresetColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BackgroundSheet(
    background: BackgroundSettings,
    onDismiss: () -> Unit,
    onBackgroundTypeChanged: (BackgroundType) -> Unit,
    onSolidColorChanged: (Long) -> Unit,
    onGradientChanged: (GradientSettings) -> Unit,
    onPatternChanged: (PatternSettings) -> Unit,
    onColorPickerRequest: (ColorPickerTarget) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(background.type.ordinal) }

    BottomSheetContainer(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            SheetHeader(title = "Background", onDismiss = onDismiss)

            // Background type tabs
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.padding(horizontal = 16.dp),
                containerColor = Color.Transparent,
                divider = {}
            ) {
                BackgroundTypeTab(
                    icon = Icons.Rounded.ColorLens,
                    label = "Solid",
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        onBackgroundTypeChanged(BackgroundType.SOLID)
                    }
                )
                BackgroundTypeTab(
                    icon = Icons.Rounded.Gradient,
                    label = "Gradient",
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        onBackgroundTypeChanged(BackgroundType.GRADIENT)
                    }
                )
                BackgroundTypeTab(
                    icon = Icons.Rounded.Pattern,
                    label = "Pattern",
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        onBackgroundTypeChanged(BackgroundType.PATTERN)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (selectedTab) {
                0 -> SolidColorContent(
                    selectedColor = background.solidColor,
                    onColorSelected = onSolidColorChanged,
                    onCustomColorRequest = { onColorPickerRequest(ColorPickerTarget.BACKGROUND_SOLID) }
                )
                1 -> GradientContent(
                    gradient = background.gradient,
                    onGradientChanged = onGradientChanged,
                    onColorPickerRequest = onColorPickerRequest
                )
                2 -> PatternContent(
                    pattern = background.pattern,
                    onPatternChanged = onPatternChanged,
                    onColorPickerRequest = onColorPickerRequest
                )
            }
        }
    }
}

@Composable
private fun BackgroundTypeTab(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Tab(
        selected = selected,
        onClick = onClick,
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    )
}

@Composable
private fun SolidColorContent(
    selectedColor: Long,
    onColorSelected: (Long) -> Unit,
    onCustomColorRequest: () -> Unit
) {
    Column {
        SectionTitle("Colors")

        // Color grid
        val colors = PresetColors.SolidColors
        val chunkedColors = colors.chunked(6)

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            chunkedColors.forEach { rowColors ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowColors.forEach { color ->
                        val colorLong = color.value.toLong()
                        val isSelected = colorLong == selectedColor
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(color)
                                .then(
                                    if (isSelected) {
                                        Modifier.border(
                                            width = 3.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    } else {
                                        Modifier.border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    }
                                )
                                .clickable { onColorSelected(colorLong) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = if (color.luminance() > 0.5f) Color.Black else Color.White
                                )
                            }
                        }
                    }
                    // Fill remaining spaces if row is not complete
                    repeat(6 - rowColors.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Custom color button
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onCustomColorRequest),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.ColorLens,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Custom Color",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun GradientContent(
    gradient: GradientSettings,
    onGradientChanged: (GradientSettings) -> Unit,
    onColorPickerRequest: (ColorPickerTarget) -> Unit
) {
    // Keep selectedColorIndex in bounds when colors list changes
    var selectedColorIndex by remember { mutableIntStateOf(0) }

    // Ensure selectedColorIndex is always valid
    val safeSelectedIndex = if (gradient.colors.isNotEmpty()) {
        selectedColorIndex.coerceIn(0, gradient.colors.lastIndex)
    } else {
        0
    }

    // Update selectedColorIndex if it was out of bounds
    if (safeSelectedIndex != selectedColorIndex) {
        selectedColorIndex = safeSelectedIndex
    }

    Column {
        // Live gradient preview
        SectionTitle("Preview")
        GradientPreview(
            gradient = gradient,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Gradient type selector
        SectionTitle("Gradient Type")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GradientType.entries.filter { it != GradientType.MESH }.forEach { type ->
                val isSelected = gradient.type == type
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onGradientChanged(gradient.copy(type = type)) },
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Gradient colors editor
        SectionTitle("Colors")
        GradientColorEditor(
            colors = gradient.colors,
            selectedIndex = safeSelectedIndex,
            onColorIndexSelected = { selectedColorIndex = it.coerceIn(0, gradient.colors.lastIndex.coerceAtLeast(0)) },
            onColorChanged = { index, color ->
                val newColors = gradient.colors.toMutableList()
                if (index < newColors.size) {
                    newColors[index] = color
                }
                onGradientChanged(gradient.copy(colors = newColors))
            },
            onAddColor = {
                if (gradient.colors.size < 5) {
                    val newColors = gradient.colors.toMutableList()
                    newColors.add(0xFFFFFFFF)
                    onGradientChanged(gradient.copy(colors = newColors))
                }
            },
            onRemoveColor = { index ->
                if (gradient.colors.size > 2 && index >= 0 && index < gradient.colors.size) {
                    val newColors = gradient.colors.toMutableList()
                    newColors.removeAt(index)
                    if (selectedColorIndex >= newColors.size) {
                        selectedColorIndex = (newColors.size - 1).coerceAtLeast(0)
                    }
                    onGradientChanged(gradient.copy(colors = newColors))
                }
            },
            onCustomColorRequest = {
                // Store selected index and open color picker
                onColorPickerRequest(ColorPickerTarget.GRADIENT_COLOR)
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Preset gradients
        SectionTitle("Presets")
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PresetColors.GradientPresets.forEach { presetColors ->
                val presetLongs = presetColors.map { it.value.toLong() }
                val isSelected = gradient.colors == presetLongs
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(presetColors)
                        )
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    width = 3.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            } else {
                                Modifier
                            }
                        )
                        .clickable {
                            onGradientChanged(gradient.copy(colors = presetLongs))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Angle slider (for linear gradient)
        if (gradient.type == GradientType.LINEAR) {
            SectionTitle("Angle")
            SliderWithValue(
                value = gradient.angle,
                onValueChange = { onGradientChanged(gradient.copy(angle = it)) },
                valueRange = 0f..360f,
                valueLabel = "${gradient.angle.toInt()}°",
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Quick angle buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0f, 45f, 90f, 135f, 180f, 225f, 270f, 315f).forEach { angle ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onGradientChanged(gradient.copy(angle = angle)) },
                        color = if (gradient.angle.toInt() == angle.toInt())
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${angle.toInt()}°",
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Center position (for radial/sweep)
        if (gradient.type == GradientType.RADIAL || gradient.type == GradientType.SWEEP) {
            SectionTitle("Center X")
            SliderWithValue(
                value = gradient.centerX,
                onValueChange = { onGradientChanged(gradient.copy(centerX = it)) },
                valueRange = 0f..1f,
                valueLabel = String.format("%.2f", gradient.centerX),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("Center Y")
            SliderWithValue(
                value = gradient.centerY,
                onValueChange = { onGradientChanged(gradient.copy(centerY = it)) },
                valueRange = 0f..1f,
                valueLabel = String.format("%.2f", gradient.centerY),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Quick position presets
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "TL" to Pair(0f, 0f),
                    "T" to Pair(0.5f, 0f),
                    "TR" to Pair(1f, 0f),
                    "L" to Pair(0f, 0.5f),
                    "C" to Pair(0.5f, 0.5f),
                    "R" to Pair(1f, 0.5f),
                    "BL" to Pair(0f, 1f),
                    "B" to Pair(0.5f, 1f),
                    "BR" to Pair(1f, 1f)
                ).forEach { (label, position) ->
                    val isSelected = gradient.centerX == position.first && gradient.centerY == position.second
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onGradientChanged(gradient.copy(centerX = position.first, centerY = position.second))
                            },
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Radius (for radial)
        if (gradient.type == GradientType.RADIAL) {
            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("Radius")
            SliderWithValue(
                value = gradient.radius,
                onValueChange = { onGradientChanged(gradient.copy(radius = it)) },
                valueRange = 0.1f..2f,
                valueLabel = String.format("%.2f", gradient.radius),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun GradientPreview(
    gradient: GradientSettings,
    modifier: Modifier = Modifier
) {
    // Ensure we have at least 2 valid colors for gradients with safe access
    val rawColors = when {
        gradient.colors.size >= 2 -> gradient.colors.mapNotNull { colorLong ->
            try { Color(colorLong.toULong()) } catch (e: Exception) { null }
        }
        gradient.colors.size == 1 -> {
            val color = try { Color(gradient.colors[0].toULong()) } catch (e: Exception) { Color.White }
            listOf(color, color)
        }
        else -> listOf(Color.White, Color.LightGray)
    }

    // Ensure we have at least 2 colors after filtering
    val safeColors = if (rawColors.size >= 2) rawColors else listOf(Color.White, Color.LightGray)

    // Use remember to cache the first color for fallback
    val fallbackColor = safeColors.firstOrNull() ?: Color.White

    Box(
        modifier = modifier
            .background(fallbackColor) // Solid fallback behind the gradient
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            // Guard against zero/negative dimensions
            if (size.width <= 0f || size.height <= 0f) return@Canvas

            val brush = try {
                when (gradient.type) {
                    GradientType.LINEAR -> {
                        val angleRad = gradient.angle * PI.toFloat() / 180f
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val len = maxOf(size.width, size.height).coerceAtLeast(1f) / 2f

                        Brush.linearGradient(
                            colors = safeColors,
                            start = androidx.compose.ui.geometry.Offset(
                                centerX - len * cos(angleRad),
                                centerY - len * sin(angleRad)
                            ),
                            end = androidx.compose.ui.geometry.Offset(
                                centerX + len * cos(angleRad),
                                centerY + len * sin(angleRad)
                            )
                        )
                    }
                    GradientType.RADIAL -> {
                        val radius = (gradient.radius * maxOf(size.width, size.height)).coerceAtLeast(1f)
                        Brush.radialGradient(
                            colors = safeColors,
                            center = androidx.compose.ui.geometry.Offset(
                                (gradient.centerX * size.width).coerceIn(0f, size.width),
                                (gradient.centerY * size.height).coerceIn(0f, size.height)
                            ),
                            radius = radius
                        )
                    }
                    GradientType.SWEEP -> {
                        Brush.sweepGradient(
                            colors = safeColors,
                            center = androidx.compose.ui.geometry.Offset(
                                (gradient.centerX * size.width).coerceIn(0f, size.width),
                                (gradient.centerY * size.height).coerceIn(0f, size.height)
                            )
                        )
                    }
                    GradientType.MESH -> Brush.linearGradient(safeColors)
                }
            } catch (e: Exception) {
                // Return null to trigger solid fallback
                null
            }

            if (brush != null) {
                try {
                    drawRect(brush = brush)
                } catch (e: Exception) {
                    // If drawing fails, draw solid fallback
                    drawRect(color = fallbackColor)
                }
            }
            // If brush is null, the solid background behind handles the fallback
        }
    }
}

@Composable
private fun GradientColorEditor(
    colors: List<Long>,
    selectedIndex: Int,
    onColorIndexSelected: (Int) -> Unit,
    onColorChanged: (Int, Long) -> Unit,
    onAddColor: () -> Unit,
    onRemoveColor: (Int) -> Unit,
    onCustomColorRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Ensure selectedIndex is always valid
    val safeIndex = if (colors.isNotEmpty()) selectedIndex.coerceIn(0, colors.lastIndex) else 0

    Column(modifier = modifier) {
        // Color stops
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            colors.forEachIndexed { index, colorLong ->
                val color = Color(colorLong.toULong())
                val isSelected = index == safeIndex

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(color)
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    width = 3.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            } else {
                                Modifier.border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        )
                        .clickable { onColorIndexSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = if (color.luminance() > 0.5f) Color.Black else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Add color button if less than 5 colors
            if (colors.size < 5) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onAddColor() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add color",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Edit selected color
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quick color presets for selected slot
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PresetColors.SolidColors.take(12).forEach { presetColor ->
                    val colorLong = presetColor.value.toLong()
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(presetColor)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            )
                            .clickable {
                                onColorChanged(safeIndex, colorLong)
                            }
                    )
                }

                // Custom color button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape
                        )
                        .clickable { onCustomColorRequest() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ColorLens,
                        contentDescription = "Custom",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Remove button (if more than 2 colors)
            if (colors.size > 2) {
                FilledTonalIconButton(
                    onClick = { onRemoveColor(safeIndex) }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Remove color"
                    )
                }
            }
        }
    }
}

@Composable
private fun PatternContent(
    pattern: PatternSettings,
    onPatternChanged: (PatternSettings) -> Unit,
    onColorPickerRequest: (ColorPickerTarget) -> Unit
) {
    Column {
        SectionTitle("Pattern Type")

        // Pattern type grid
        val patternTypes = listOf(
            PatternType.NONE to "None",
            PatternType.DOTS to "Dots",
            PatternType.GRID to "Grid",
            PatternType.DIAGONAL_LINES to "Diagonal",
            PatternType.CROSS_HATCH to "Crosshatch",
            PatternType.WAVES to "Waves",
            PatternType.CIRCLES to "Circles",
            PatternType.HEXAGONS to "Hexagons",
            PatternType.TRIANGLES to "Triangles",
            PatternType.CHEVRON to "Chevron",
            PatternType.DIAMOND to "Diamond",
            PatternType.SCATTERED_DOTS to "Scattered",
            PatternType.PARALLEL_LINES to "Lines",
            PatternType.CORNER_ACCENT to "Corner",
            PatternType.SOFT_SHAPES to "Soft",
            PatternType.ORGANIC_BLOBS to "Blobs",
            PatternType.FRAME to "Frame"
        )

        val chunkedPatterns = patternTypes.chunked(4)
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            chunkedPatterns.forEach { rowPatterns ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowPatterns.forEach { (type, name) ->
                        val isSelected = pattern.type == type
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onPatternChanged(pattern.copy(type = type)) },
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    repeat(4 - rowPatterns.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        if (pattern.type != PatternType.NONE) {
            Spacer(modifier = Modifier.height(24.dp))

            // Background color
            SectionTitle("Background Color")
            ColorPresetRow(
                selectedColor = pattern.backgroundColor,
                colors = PresetColors.SolidColors.take(12).map { it.value.toLong() },
                onColorSelected = { onPatternChanged(pattern.copy(backgroundColor = it)) },
                onCustomColorRequest = { onColorPickerRequest(ColorPickerTarget.PATTERN_SECONDARY) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Pattern color
            SectionTitle("Pattern Color")
            ColorPresetRow(
                selectedColor = pattern.primaryColor,
                colors = PresetColors.SolidColors.take(12).map { it.value.toLong() },
                onColorSelected = { onPatternChanged(pattern.copy(primaryColor = it)) },
                onCustomColorRequest = { onColorPickerRequest(ColorPickerTarget.PATTERN_PRIMARY) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Pattern scale
            SectionTitle("Scale")
            SliderWithValue(
                value = pattern.scale,
                onValueChange = { onPatternChanged(pattern.copy(scale = it)) },
                valueRange = 0.5f..3f,
                valueLabel = String.format("%.1fx", pattern.scale),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pattern opacity
            SectionTitle("Opacity")
            SliderWithValue(
                value = pattern.opacity,
                onValueChange = { onPatternChanged(pattern.copy(opacity = it)) },
                valueRange = 0.1f..1f,
                valueLabel = "${(pattern.opacity * 100).toInt()}%",
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Density for applicable patterns
            if (pattern.type in listOf(PatternType.DOTS, PatternType.SCATTERED_DOTS)) {
                Spacer(modifier = Modifier.height(16.dp))

                SectionTitle("Density")
                SliderWithValue(
                    value = pattern.density,
                    onValueChange = { onPatternChanged(pattern.copy(density = it)) },
                    valueRange = 0.2f..2f,
                    valueLabel = String.format("%.1fx", pattern.density),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Rotation for line patterns
            if (pattern.type in listOf(PatternType.DIAGONAL_LINES, PatternType.PARALLEL_LINES)) {
                Spacer(modifier = Modifier.height(16.dp))

                SectionTitle("Rotation")
                SliderWithValue(
                    value = pattern.rotation,
                    onValueChange = { onPatternChanged(pattern.copy(rotation = it)) },
                    valueRange = 0f..180f,
                    valueLabel = "${pattern.rotation.toInt()}°",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

private fun Color.luminance(): Float {
    return (0.299f * red + 0.587f * green + 0.114f * blue)
}
