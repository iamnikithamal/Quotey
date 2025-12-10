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
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quotey.create.data.model.ShapeElement
import com.quotey.create.data.model.ShapeStyle
import com.quotey.create.data.model.ShapeType
import com.quotey.create.ui.screens.editor.ColorPickerTarget
import com.quotey.create.ui.theme.PresetColors

@Composable
fun ShapeSheet(
    shapeElement: ShapeElement?,
    onDismiss: () -> Unit,
    onAddShape: (ShapeType) -> Unit,
    onUpdateStyle: ((ShapeStyle) -> ShapeStyle) -> Unit,
    onColorPickerRequest: (ColorPickerTarget) -> Unit,
    onDelete: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(if (shapeElement != null) 1 else 0) }

    BottomSheetContainer(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            SheetHeader(
                title = if (shapeElement != null) "Edit Shape" else "Add Shape",
                onDismiss = onDismiss
            )

            // Tab selector if shape is selected
            if (shapeElement != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Add New", "Style").forEachIndexed { index, label ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedTab = index },
                            color = if (selectedTab == index)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selectedTab == index)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            when {
                selectedTab == 0 || shapeElement == null -> {
                    ShapeTypeSelector(onAddShape = onAddShape)
                }
                else -> {
                    ShapeStyleEditor(
                        style = shapeElement.style,
                        shapeType = shapeElement.type,
                        onUpdateStyle = onUpdateStyle,
                        onColorPickerRequest = onColorPickerRequest
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Delete button
                    Button(
                        onClick = onDelete,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Shape")
                    }
                }
            }
        }
    }
}

@Composable
private fun ShapeTypeSelector(
    onAddShape: (ShapeType) -> Unit
) {
    Column {
        SectionTitle("Basic Shapes")

        val basicShapes = listOf(
            ShapeType.RECTANGLE to "Rectangle",
            ShapeType.ROUNDED_RECTANGLE to "Rounded",
            ShapeType.CIRCLE to "Circle",
            ShapeType.OVAL to "Oval",
            ShapeType.TRIANGLE to "Triangle",
            ShapeType.DIAMOND to "Diamond"
        )

        val basicChunked = basicShapes.chunked(3)
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            basicChunked.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { (type, name) ->
                        ShapeTypeButton(
                            type = type,
                            name = name,
                            onClick = { onAddShape(type) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(3 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        SectionTitle("Special Shapes")

        val specialShapes = listOf(
            ShapeType.STAR to "Star",
            ShapeType.HEART to "Heart",
            ShapeType.HEXAGON to "Hexagon",
            ShapeType.PENTAGON to "Pentagon",
            ShapeType.CROSS to "Cross",
            ShapeType.RING to "Ring"
        )

        val specialChunked = specialShapes.chunked(3)
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            specialChunked.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { (type, name) ->
                        ShapeTypeButton(
                            type = type,
                            name = name,
                            onClick = { onAddShape(type) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(3 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        SectionTitle("Lines & Arrows")

        val lineShapes = listOf(
            ShapeType.LINE to "Line",
            ShapeType.ARROW to "Arrow",
            ShapeType.ARC to "Arc"
        )

        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            lineShapes.forEach { (type, name) ->
                ShapeTypeButton(
                    type = type,
                    name = name,
                    onClick = { onAddShape(type) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ShapeTypeButton(
    type: ShapeType,
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Shape preview icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(getShapeForType(type))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = getShapeForType(type)
                    )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun getShapeForType(type: ShapeType) = when (type) {
    ShapeType.CIRCLE -> CircleShape
    ShapeType.ROUNDED_RECTANGLE -> RoundedCornerShape(8.dp)
    else -> RoundedCornerShape(4.dp)
}

@Composable
private fun ShapeStyleEditor(
    style: ShapeStyle,
    shapeType: ShapeType,
    onUpdateStyle: ((ShapeStyle) -> ShapeStyle) -> Unit,
    onColorPickerRequest: (ColorPickerTarget) -> Unit
) {
    Column {
        // Fill Color
        SectionTitle("Fill Color")
        ColorPresetRow(
            selectedColor = style.fillColor ?: 0x00000000,
            colors = PresetColors.SolidColors.take(12).map { it.value.toLong() },
            onColorSelected = { color ->
                onUpdateStyle { it.copy(fillColor = color) }
            },
            onCustomColorRequest = { onColorPickerRequest(ColorPickerTarget.SHAPE_FILL) },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Option to remove fill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onUpdateStyle { it.copy(fillColor = null) } }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(4.dp)
                    )
            ) {
                // Diagonal line to indicate "no fill"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.error)
                        .align(Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "No Fill (Transparent)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stroke Color
        SectionTitle("Stroke Color")
        ColorPresetRow(
            selectedColor = style.strokeColor,
            colors = PresetColors.SolidColors.take(12).map { it.value.toLong() },
            onColorSelected = { color ->
                onUpdateStyle { it.copy(strokeColor = color) }
            },
            onCustomColorRequest = { onColorPickerRequest(ColorPickerTarget.SHAPE_STROKE) },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Stroke Width
        SectionTitle("Stroke Width")
        SliderWithValue(
            value = style.strokeWidth,
            onValueChange = { onUpdateStyle { s -> s.copy(strokeWidth = it) } },
            valueRange = 0f..20f,
            valueLabel = "${style.strokeWidth.toInt()}px",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Corner Radius (for applicable shapes)
        if (shapeType == ShapeType.RECTANGLE || shapeType == ShapeType.ROUNDED_RECTANGLE) {
            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("Corner Radius")
            SliderWithValue(
                value = style.cornerRadius,
                onValueChange = { onUpdateStyle { s -> s.copy(cornerRadius = it) } },
                valueRange = 0f..100f,
                valueLabel = "${style.cornerRadius.toInt()}px",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Sides (for polygon)
        if (shapeType == ShapeType.POLYGON) {
            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("Number of Sides")
            SliderWithValue(
                value = style.sides.toFloat(),
                onValueChange = { onUpdateStyle { s -> s.copy(sides = it.toInt()) } },
                valueRange = 3f..12f,
                valueLabel = "${style.sides}",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Inner radius (for star)
        if (shapeType == ShapeType.STAR) {
            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("Star Points")
            SliderWithValue(
                value = style.sides.toFloat(),
                onValueChange = { onUpdateStyle { s -> s.copy(sides = it.toInt()) } },
                valueRange = 3f..12f,
                valueLabel = "${style.sides} points",
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("Inner Radius")
            SliderWithValue(
                value = style.innerRadius,
                onValueChange = { onUpdateStyle { s -> s.copy(innerRadius = it) } },
                valueRange = 0.1f..0.9f,
                valueLabel = String.format("%.1f", style.innerRadius),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Opacity
        SectionTitle("Opacity")
        SliderWithValue(
            value = style.opacity,
            onValueChange = { onUpdateStyle { s -> s.copy(opacity = it) } },
            valueRange = 0f..1f,
            valueLabel = "${(style.opacity * 100).toInt()}%",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Shadow toggle
        SectionTitle("Shadow")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(false to "Off", true to "On").forEach { (enabled, label) ->
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onUpdateStyle { it.copy(shadowEnabled = enabled) } },
                    color = if (style.shadowEnabled == enabled)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (style.shadowEnabled == enabled)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (style.shadowEnabled) {
            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("Shadow Blur")
            SliderWithValue(
                value = style.shadowBlur,
                onValueChange = { onUpdateStyle { s -> s.copy(shadowBlur = it) } },
                valueRange = 0f..50f,
                valueLabel = "${style.shadowBlur.toInt()}px",
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("Shadow Offset")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "X: ${style.shadowOffsetX.toInt()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = style.shadowOffsetX,
                        onValueChange = { onUpdateStyle { s -> s.copy(shadowOffsetX = it) } },
                        valueRange = -30f..30f
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Y: ${style.shadowOffsetY.toInt()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = style.shadowOffsetY,
                        onValueChange = { onUpdateStyle { s -> s.copy(shadowOffsetY = it) } },
                        valueRange = -30f..30f
                    )
                }
            }
        }
    }
}
