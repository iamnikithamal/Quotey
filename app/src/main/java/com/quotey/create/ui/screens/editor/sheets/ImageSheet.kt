package com.quotey.create.ui.screens.editor.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Flip
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quotey.create.data.model.ImageElement
import com.quotey.create.data.model.ImageScaleType
import com.quotey.create.data.model.ImageStyle
import com.quotey.create.ui.screens.editor.ColorPickerTarget
import com.quotey.create.ui.theme.PresetColors

@Composable
fun ImageSheet(
    imageElement: ImageElement?,
    onDismiss: () -> Unit,
    onAddImage: () -> Unit,
    onUpdateStyle: ((ImageStyle) -> ImageStyle) -> Unit,
    onColorPickerRequest: (ColorPickerTarget) -> Unit,
    onDelete: () -> Unit
) {
    BottomSheetContainer(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            SheetHeader(
                title = if (imageElement != null) "Edit Image" else "Add Image",
                onDismiss = onDismiss
            )

            if (imageElement == null) {
                // Add image option
                AddImageSection(onAddImage = onAddImage)
            } else {
                // Image style editor
                ImageStyleEditor(
                    style = imageElement.style,
                    onUpdateStyle = onUpdateStyle,
                    onColorPickerRequest = onColorPickerRequest
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Replace image button
                Button(
                    onClick = onAddImage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Image,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Replace Image")
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                    Text("Delete Image")
                }
            }
        }
    }
}

@Composable
private fun AddImageSection(
    onAddImage: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.5f)
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onAddImage),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Add Image from Gallery",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tap to select an image from your device",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Supported formats: JPG, PNG, WebP",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ImageStyleEditor(
    style: ImageStyle,
    onUpdateStyle: ((ImageStyle) -> ImageStyle) -> Unit,
    onColorPickerRequest: (ColorPickerTarget) -> Unit
) {
    Column {
        // Scale Type
        SectionTitle("Scale Mode")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ImageScaleType.entries.forEach { scaleType ->
                val isSelected = style.scaleType == scaleType
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onUpdateStyle { it.copy(scaleType = scaleType) } },
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = scaleType.name.lowercase().replaceFirstChar { it.uppercase() },
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

        Spacer(modifier = Modifier.height(20.dp))

        // Corner Radius
        SectionTitle("Corner Radius")
        SliderWithValue(
            value = style.cornerRadius,
            onValueChange = { onUpdateStyle { s -> s.copy(cornerRadius = it) } },
            valueRange = 0f..100f,
            valueLabel = "${style.cornerRadius.toInt()}px",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Border
        SectionTitle("Border Width")
        SliderWithValue(
            value = style.borderWidth,
            onValueChange = { onUpdateStyle { s -> s.copy(borderWidth = it) } },
            valueRange = 0f..20f,
            valueLabel = "${style.borderWidth.toInt()}px",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (style.borderWidth > 0) {
            Spacer(modifier = Modifier.height(12.dp))

            SectionTitle("Border Color")
            ColorPresetRow(
                selectedColor = style.borderColor,
                colors = PresetColors.SolidColors.take(12).map { it.value.toLong() },
                onColorSelected = { color ->
                    onUpdateStyle { it.copy(borderColor = color) }
                },
                onCustomColorRequest = { onColorPickerRequest(ColorPickerTarget.IMAGE_BORDER) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Opacity
        SectionTitle("Opacity")
        SliderWithValue(
            value = style.opacity,
            onValueChange = { onUpdateStyle { s -> s.copy(opacity = it) } },
            valueRange = 0f..1f,
            valueLabel = "${(style.opacity * 100).toInt()}%",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Flip options
        SectionTitle("Flip")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onUpdateStyle { it.copy(flipHorizontal = !it.flipHorizontal) } },
                color = if (style.flipHorizontal)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Flip,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (style.flipHorizontal)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Horizontal",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (style.flipHorizontal)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onUpdateStyle { it.copy(flipVertical = !it.flipVertical) } },
                color = if (style.flipVertical)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Flip,
                        contentDescription = null,
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer { rotationZ = 90f },
                        tint = if (style.flipVertical)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Vertical",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (style.flipVertical)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

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
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Image adjustments
        SectionTitle("Adjustments")

        // Brightness
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Brightness",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(80.dp)
            )
            Slider(
                value = style.brightness,
                onValueChange = { onUpdateStyle { s -> s.copy(brightness = it) } },
                valueRange = 0.5f..1.5f,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = String.format("%.1f", style.brightness),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.End
            )
        }

        // Contrast
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Contrast",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(80.dp)
            )
            Slider(
                value = style.contrast,
                onValueChange = { onUpdateStyle { s -> s.copy(contrast = it) } },
                valueRange = 0.5f..1.5f,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = String.format("%.1f", style.contrast),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.End
            )
        }

        // Saturation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Saturation",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(80.dp)
            )
            Slider(
                value = style.saturation,
                onValueChange = { onUpdateStyle { s -> s.copy(saturation = it) } },
                valueRange = 0f..2f,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = String.format("%.1f", style.saturation),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.End
            )
        }

        // Blur
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Blur",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(80.dp)
            )
            Slider(
                value = style.blur,
                onValueChange = { onUpdateStyle { s -> s.copy(blur = it) } },
                valueRange = 0f..20f,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${style.blur.toInt()}px",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.End
            )
        }

        // Reset adjustments button
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    onUpdateStyle {
                        it.copy(
                            brightness = 1f,
                            contrast = 1f,
                            saturation = 1f,
                            blur = 0f
                        )
                    }
                },
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Reset Adjustments",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(12.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

// Extension to use graphicsLayer
private fun Modifier.graphicsLayer(block: androidx.compose.ui.graphics.GraphicsLayerScope.() -> Unit): Modifier =
    this.then(androidx.compose.ui.Modifier.graphicsLayer(block))
