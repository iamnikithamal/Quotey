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
import androidx.compose.material.icons.automirrored.rounded.FormatAlignLeft
import androidx.compose.material.icons.automirrored.rounded.FormatAlignRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FormatAlignCenter
import androidx.compose.material.icons.rounded.FormatAlignJustify
import androidx.compose.material.icons.rounded.FormatBold
import androidx.compose.material.icons.rounded.FormatItalic
import androidx.compose.material.icons.rounded.FormatStrikethrough
import androidx.compose.material.icons.rounded.FormatUnderlined
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quotey.create.data.model.TextAlignment
import com.quotey.create.data.model.TextDecorationStyle
import com.quotey.create.data.model.TextElement
import com.quotey.create.data.model.TextFontStyle
import com.quotey.create.data.model.TextStyle as QuoteyTextStyle
import com.quotey.create.ui.theme.PresetColors

@Composable
fun TextSheet(
    textElement: TextElement?,
    onDismiss: () -> Unit,
    onUpdateStyle: (update: (QuoteyTextStyle) -> QuoteyTextStyle) -> Unit,
    onUpdateContent: (String) -> Unit,
    onColorPickerRequest: () -> Unit,
    onDelete: () -> Unit
) {
    if (textElement == null) {
        EmptyTextSheet(onDismiss = onDismiss)
        return
    }

    val style = textElement.style
    var content by remember(textElement.id) { mutableStateOf(textElement.content) }

    BottomSheetContainer(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // Header
            SheetHeader(
                title = "Text Settings",
                onDismiss = onDismiss,
                actions = {
                    IconButton(
                        onClick = onDelete,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Text content
            SectionTitle("Content")
            OutlinedTextField(
                value = content,
                onValueChange = {
                    content = it
                    onUpdateContent(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Enter your text...") },
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Font Size
            SectionTitle("Font Size")
            SliderWithValue(
                value = style.fontSize,
                onValueChange = { onUpdateStyle { it.copy(fontSize = it2) } },
                valueRange = 12f..120f,
                valueLabel = "${style.fontSize.toInt()} sp",
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Font Weight
            SectionTitle("Font Weight")
            FontWeightSelector(
                currentWeight = style.fontWeight,
                onWeightSelected = { weight ->
                    onUpdateStyle { it.copy(fontWeight = weight) }
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Text Style (Bold, Italic, etc.)
            SectionTitle("Style")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StyleToggleButton(
                    icon = Icons.Rounded.FormatBold,
                    isSelected = style.fontWeight >= 700,
                    onClick = {
                        onUpdateStyle {
                            it.copy(fontWeight = if (it.fontWeight >= 700) 400 else 700)
                        }
                    }
                )
                StyleToggleButton(
                    icon = Icons.Rounded.FormatItalic,
                    isSelected = style.fontStyle == TextFontStyle.ITALIC,
                    onClick = {
                        onUpdateStyle {
                            it.copy(
                                fontStyle = if (it.fontStyle == TextFontStyle.ITALIC)
                                    TextFontStyle.NORMAL else TextFontStyle.ITALIC
                            )
                        }
                    }
                )
                StyleToggleButton(
                    icon = Icons.Rounded.FormatUnderlined,
                    isSelected = style.textDecoration == TextDecorationStyle.UNDERLINE,
                    onClick = {
                        onUpdateStyle {
                            it.copy(
                                textDecoration = if (it.textDecoration == TextDecorationStyle.UNDERLINE)
                                    TextDecorationStyle.NONE else TextDecorationStyle.UNDERLINE
                            )
                        }
                    }
                )
                StyleToggleButton(
                    icon = Icons.Rounded.FormatStrikethrough,
                    isSelected = style.textDecoration == TextDecorationStyle.LINE_THROUGH,
                    onClick = {
                        onUpdateStyle {
                            it.copy(
                                textDecoration = if (it.textDecoration == TextDecorationStyle.LINE_THROUGH)
                                    TextDecorationStyle.NONE else TextDecorationStyle.LINE_THROUGH
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Text Alignment
            SectionTitle("Alignment")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StyleToggleButton(
                    icon = Icons.AutoMirrored.Rounded.FormatAlignLeft,
                    isSelected = style.textAlign == TextAlignment.LEFT,
                    onClick = { onUpdateStyle { it.copy(textAlign = TextAlignment.LEFT) } }
                )
                StyleToggleButton(
                    icon = Icons.Rounded.FormatAlignCenter,
                    isSelected = style.textAlign == TextAlignment.CENTER,
                    onClick = { onUpdateStyle { it.copy(textAlign = TextAlignment.CENTER) } }
                )
                StyleToggleButton(
                    icon = Icons.AutoMirrored.Rounded.FormatAlignRight,
                    isSelected = style.textAlign == TextAlignment.RIGHT,
                    onClick = { onUpdateStyle { it.copy(textAlign = TextAlignment.RIGHT) } }
                )
                StyleToggleButton(
                    icon = Icons.Rounded.FormatAlignJustify,
                    isSelected = style.textAlign == TextAlignment.JUSTIFY,
                    onClick = { onUpdateStyle { it.copy(textAlign = TextAlignment.JUSTIFY) } }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Text Color
            SectionTitle("Color")
            ColorPresetRow(
                selectedColor = style.color,
                colors = PresetColors.SolidColors.map { it.value.toLong() },
                onColorSelected = { color ->
                    onUpdateStyle { it.copy(color = color) }
                },
                onCustomColorRequest = onColorPickerRequest,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Line Height
            SectionTitle("Line Height")
            SliderWithValue(
                value = style.lineHeight,
                onValueChange = { onUpdateStyle { s -> s.copy(lineHeight = it) } },
                valueRange = 1f..3f,
                valueLabel = String.format("%.2f", style.lineHeight),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Letter Spacing
            SectionTitle("Letter Spacing")
            SliderWithValue(
                value = style.letterSpacing,
                onValueChange = { onUpdateStyle { s -> s.copy(letterSpacing = it) } },
                valueRange = -5f..20f,
                valueLabel = String.format("%.1f sp", style.letterSpacing),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun EmptyTextSheet(onDismiss: () -> Unit) {
    BottomSheetContainer(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SheetHeader(title = "Text Settings", onDismiss = onDismiss)

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "No text element selected",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Tap on a text element to edit it",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FontWeightSelector(
    currentWeight: Int,
    onWeightSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val weights = listOf(
        100 to "Thin",
        200 to "ExtraLight",
        300 to "Light",
        400 to "Regular",
        500 to "Medium",
        600 to "SemiBold",
        700 to "Bold",
        800 to "ExtraBold",
        900 to "Black"
    )

    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        weights.forEach { (weight, name) ->
            val isSelected = currentWeight == weight
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onWeightSelected(weight) },
                color = if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight(weight),
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun StyleToggleButton(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun SliderWithValue(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    valueLabel: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(60.dp)
            )
        }
    }
}

@Composable
fun ColorPresetRow(
    selectedColor: Long,
    colors: List<Long>,
    onColorSelected: (Long) -> Unit,
    onCustomColorRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        colors.forEach { color ->
            val isSelected = color == selectedColor
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(color.toULong()))
                    .then(
                        if (isSelected) {
                            Modifier.border(
                                width = 3.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                        } else {
                            Modifier.border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            )
                        }
                    )
                    .clickable { onColorSelected(color) }
            )
        }

        // Custom color button
        Surface(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onCustomColorRequest),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun BottomSheetContainer(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false, onClick = {}),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            content()
        }
    }
}

@Composable
fun SheetHeader(
    title: String,
    onDismiss: () -> Unit,
    actions: @Composable () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Handle bar
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 12.dp)
                .width(40.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Rounded.Close, contentDescription = "Close")
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row {
                actions()
            }
        }
    }
}
