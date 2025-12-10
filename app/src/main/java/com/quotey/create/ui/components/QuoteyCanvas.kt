package com.quotey.create.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quotey.create.data.model.*
import com.quotey.create.ui.theme.PoppinsFamily
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun QuoteyCanvas(
    page: QuoteyPage,
    selectedElementId: String?,
    onElementSelected: (String?) -> Unit,
    onElementPositionChanged: (String, ElementPosition) -> Unit,
    onTextContentChanged: (String, String) -> Unit,
    onBackgroundTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canvas = page.canvas
    val background = page.background
    val density = LocalDensity.current

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(canvas.aspectRatio.ratio)
            .shadow(
                elevation = if (canvas.shadowEnabled) canvas.shadowBlur.dp else 8.dp,
                shape = RoundedCornerShape(canvas.cornerRadius.dp),
                spotColor = Color(canvas.shadowColor)
            )
            .clip(RoundedCornerShape(canvas.cornerRadius.dp))
            .onSizeChanged { canvasSize = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onBackgroundTap() }
                )
            }
    ) {
        // Background layer
        BackgroundLayer(
            background = background,
            cornerRadius = canvas.cornerRadius,
            modifier = Modifier.fillMaxSize()
        )

        // Text elements layer
        page.textElements.forEach { element ->
            TextElementView(
                element = element,
                isSelected = element.id == selectedElementId,
                canvasSize = canvasSize,
                padding = canvas.padding,
                onSelect = { onElementSelected(element.id) },
                onPositionChanged = { position ->
                    onElementPositionChanged(element.id, position)
                },
                onContentChanged = { content ->
                    onTextContentChanged(element.id, content)
                }
            )
        }
    }
}

@Composable
private fun BackgroundLayer(
    background: BackgroundSettings,
    cornerRadius: Float,
    modifier: Modifier = Modifier
) {
    when (background.type) {
        BackgroundType.SOLID -> {
            Box(
                modifier = modifier.background(
                    color = Color(background.solidColor),
                    shape = RoundedCornerShape(cornerRadius.dp)
                )
            )
        }

        BackgroundType.GRADIENT -> {
            val gradient = background.gradient
            val colors = gradient.colors.map { Color(it) }
            val brush = when (gradient.type) {
                GradientType.LINEAR -> {
                    val angleRad = gradient.angle * PI.toFloat() / 180f
                    Brush.linearGradient(
                        colors = colors,
                        start = Offset(
                            0.5f - cos(angleRad) * 0.5f,
                            0.5f - sin(angleRad) * 0.5f
                        ),
                        end = Offset(
                            0.5f + cos(angleRad) * 0.5f,
                            0.5f + sin(angleRad) * 0.5f
                        )
                    )
                }
                GradientType.RADIAL -> {
                    Brush.radialGradient(
                        colors = colors,
                        center = Offset(gradient.centerX, gradient.centerY),
                        radius = gradient.radius * 1000f
                    )
                }
                GradientType.SWEEP -> {
                    Brush.sweepGradient(
                        colors = colors,
                        center = Offset(gradient.centerX * 1000f, gradient.centerY * 1000f)
                    )
                }
                GradientType.MESH -> {
                    // Mesh gradient approximation using multiple radial gradients
                    Brush.linearGradient(colors = colors)
                }
            }
            Box(
                modifier = modifier.background(
                    brush = brush,
                    shape = RoundedCornerShape(cornerRadius.dp)
                )
            )
        }

        BackgroundType.PATTERN -> {
            Box(modifier = modifier) {
                // Base color
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                )
                // Pattern overlay
                PatternOverlay(
                    pattern = background.pattern,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        BackgroundType.IMAGE -> {
            // TODO: Implement image background
            Box(
                modifier = modifier.background(
                    color = Color(background.solidColor),
                    shape = RoundedCornerShape(cornerRadius.dp)
                )
            )
        }
    }
}

@Composable
private fun PatternOverlay(
    pattern: PatternSettings,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(pattern.primaryColor)
    val secondaryColor = Color(pattern.secondaryColor)

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        when (pattern.type) {
            PatternType.DOTS -> {
                drawDotsPattern(
                    color = primaryColor.copy(alpha = pattern.opacity),
                    density = pattern.density,
                    scale = pattern.scale
                )
            }
            PatternType.GRID -> {
                drawGridPattern(
                    color = primaryColor.copy(alpha = pattern.opacity),
                    scale = pattern.scale
                )
            }
            PatternType.DIAGONAL_LINES -> {
                drawDiagonalLinesPattern(
                    color = primaryColor.copy(alpha = pattern.opacity),
                    scale = pattern.scale,
                    rotation = pattern.rotation
                )
            }
            PatternType.WAVES -> {
                drawWavesPattern(
                    color = primaryColor.copy(alpha = pattern.opacity),
                    scale = pattern.scale
                )
            }
            PatternType.CIRCLES -> {
                drawCirclesPattern(
                    color = primaryColor.copy(alpha = pattern.opacity),
                    scale = pattern.scale
                )
            }
            PatternType.HEXAGONS -> {
                drawHexagonPattern(
                    color = primaryColor.copy(alpha = pattern.opacity),
                    scale = pattern.scale
                )
            }
            PatternType.TRIANGLES -> {
                drawTrianglePattern(
                    color = primaryColor.copy(alpha = pattern.opacity),
                    scale = pattern.scale
                )
            }
            PatternType.ORGANIC_BLOBS -> {
                drawOrganicBlobsPattern(
                    primaryColor = primaryColor.copy(alpha = pattern.opacity),
                    secondaryColor = secondaryColor.copy(alpha = pattern.opacity * 0.5f),
                    scale = pattern.scale
                )
            }
            PatternType.SCATTERED_DOTS -> {
                drawScatteredDotsPattern(
                    color = primaryColor.copy(alpha = pattern.opacity),
                    density = pattern.density,
                    scale = pattern.scale
                )
            }
            PatternType.PARALLEL_LINES -> {
                drawParallelLinesPattern(
                    color = primaryColor.copy(alpha = pattern.opacity),
                    scale = pattern.scale,
                    rotation = pattern.rotation
                )
            }
            PatternType.CORNER_ACCENT -> {
                drawCornerAccentPattern(
                    color = primaryColor.copy(alpha = pattern.opacity),
                    scale = pattern.scale
                )
            }
            PatternType.SOFT_SHAPES -> {
                drawSoftShapesPattern(
                    primaryColor = primaryColor.copy(alpha = pattern.opacity),
                    secondaryColor = secondaryColor.copy(alpha = pattern.opacity * 0.3f),
                    scale = pattern.scale
                )
            }
            else -> { /* No pattern */ }
        }
    }
}

private fun DrawScope.drawDotsPattern(
    color: Color,
    density: Float,
    scale: Float
) {
    val spacing = (40f / density) * scale
    val dotRadius = 3f * scale

    var x = spacing / 2
    while (x < size.width) {
        var y = spacing / 2
        while (y < size.height) {
            drawCircle(
                color = color,
                radius = dotRadius,
                center = Offset(x, y)
            )
            y += spacing
        }
        x += spacing
    }
}

private fun DrawScope.drawGridPattern(
    color: Color,
    scale: Float
) {
    val spacing = 50f * scale
    val strokeWidth = 1f * scale

    var x = 0f
    while (x <= size.width) {
        drawLine(
            color = color,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = strokeWidth
        )
        x += spacing
    }

    var y = 0f
    while (y <= size.height) {
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = strokeWidth
        )
        y += spacing
    }
}

private fun DrawScope.drawDiagonalLinesPattern(
    color: Color,
    scale: Float,
    rotation: Float
) {
    val spacing = 30f * scale
    val strokeWidth = 1.5f * scale
    val maxDimension = maxOf(size.width, size.height) * 2

    rotate(rotation + 45f) {
        var offset = -maxDimension
        while (offset < maxDimension) {
            drawLine(
                color = color,
                start = Offset(offset, -maxDimension),
                end = Offset(offset, maxDimension),
                strokeWidth = strokeWidth
            )
            offset += spacing
        }
    }
}

private fun DrawScope.drawWavesPattern(
    color: Color,
    scale: Float
) {
    val amplitude = 20f * scale
    val wavelength = 80f * scale
    val spacing = 40f * scale
    val strokeWidth = 2f * scale

    var yOffset = 0f
    while (yOffset < size.height + amplitude) {
        val path = Path()
        path.moveTo(0f, yOffset)

        var x = 0f
        while (x < size.width) {
            val y = yOffset + amplitude * sin(x / wavelength * 2 * PI.toFloat())
            path.lineTo(x, y)
            x += 2f
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        yOffset += spacing
    }
}

private fun DrawScope.drawCirclesPattern(
    color: Color,
    scale: Float
) {
    val spacing = 80f * scale
    val radius = 25f * scale
    val strokeWidth = 1.5f * scale

    var x = spacing / 2
    while (x < size.width + spacing) {
        var y = spacing / 2
        while (y < size.height + spacing) {
            drawCircle(
                color = color,
                radius = radius,
                center = Offset(x, y),
                style = Stroke(width = strokeWidth)
            )
            y += spacing
        }
        x += spacing
    }
}

private fun DrawScope.drawHexagonPattern(
    color: Color,
    scale: Float
) {
    val hexSize = 30f * scale
    val strokeWidth = 1f * scale
    val horizontalSpacing = hexSize * 1.5f
    val verticalSpacing = hexSize * 1.732f

    var row = 0
    var y = 0f
    while (y < size.height + hexSize) {
        var x = if (row % 2 == 0) 0f else horizontalSpacing / 2
        while (x < size.width + hexSize) {
            drawHexagon(Offset(x, y), hexSize, color, strokeWidth)
            x += horizontalSpacing
        }
        y += verticalSpacing / 2
        row++
    }
}

private fun DrawScope.drawHexagon(
    center: Offset,
    size: Float,
    color: Color,
    strokeWidth: Float
) {
    val path = Path()
    for (i in 0..5) {
        val angle = (60 * i - 30) * PI.toFloat() / 180f
        val x = center.x + size * cos(angle)
        val y = center.y + size * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color, style = Stroke(width = strokeWidth))
}

private fun DrawScope.drawTrianglePattern(
    color: Color,
    scale: Float
) {
    val triangleSize = 40f * scale
    val strokeWidth = 1f * scale

    var row = 0
    var y = 0f
    while (y < size.height + triangleSize) {
        var x = if (row % 2 == 0) 0f else triangleSize / 2
        while (x < size.width + triangleSize) {
            val path = Path()
            path.moveTo(x, y + triangleSize)
            path.lineTo(x + triangleSize / 2, y)
            path.lineTo(x + triangleSize, y + triangleSize)
            path.close()
            drawPath(path, color, style = Stroke(width = strokeWidth))
            x += triangleSize
        }
        y += triangleSize * 0.866f
        row++
    }
}

private fun DrawScope.drawOrganicBlobsPattern(
    primaryColor: Color,
    secondaryColor: Color,
    scale: Float
) {
    val random = Random(42) // Fixed seed for consistency

    // Draw several organic blobs
    repeat(5) { i ->
        val centerX = random.nextFloat() * size.width
        val centerY = random.nextFloat() * size.height
        val blobSize = (100f + random.nextFloat() * 150f) * scale
        val color = if (i % 2 == 0) primaryColor else secondaryColor

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color, color.copy(alpha = 0f)),
                center = Offset(centerX, centerY),
                radius = blobSize
            ),
            radius = blobSize,
            center = Offset(centerX, centerY)
        )
    }
}

private fun DrawScope.drawScatteredDotsPattern(
    color: Color,
    density: Float,
    scale: Float
) {
    val random = Random(123)
    val dotCount = (100 * density).toInt()

    repeat(dotCount) {
        val x = random.nextFloat() * size.width
        val y = random.nextFloat() * size.height
        val radius = (2f + random.nextFloat() * 4f) * scale

        drawCircle(
            color = color.copy(alpha = 0.3f + random.nextFloat() * 0.5f),
            radius = radius,
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawParallelLinesPattern(
    color: Color,
    scale: Float,
    rotation: Float
) {
    val spacing = 20f * scale
    val strokeWidth = 1f * scale

    rotate(rotation) {
        var y = -size.height
        while (y < size.height * 2) {
            drawLine(
                color = color,
                start = Offset(-size.width, y),
                end = Offset(size.width * 2, y),
                strokeWidth = strokeWidth
            )
            y += spacing
        }
    }
}

private fun DrawScope.drawCornerAccentPattern(
    color: Color,
    scale: Float
) {
    val accentSize = 150f * scale

    // Top-left corner
    val topLeftPath = Path().apply {
        moveTo(0f, accentSize)
        quadraticTo(0f, 0f, accentSize, 0f)
        lineTo(0f, 0f)
        close()
    }
    drawPath(topLeftPath, color)

    // Bottom-right corner
    val bottomRightPath = Path().apply {
        moveTo(size.width - accentSize, size.height)
        quadraticTo(size.width, size.height, size.width, size.height - accentSize)
        lineTo(size.width, size.height)
        close()
    }
    drawPath(bottomRightPath, color)
}

private fun DrawScope.drawSoftShapesPattern(
    primaryColor: Color,
    secondaryColor: Color,
    scale: Float
) {
    // Soft blurred shapes in corners and edges
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(primaryColor, primaryColor.copy(alpha = 0f)),
            center = Offset(0f, 0f),
            radius = 200f * scale
        ),
        radius = 200f * scale,
        center = Offset(0f, 0f)
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(secondaryColor, secondaryColor.copy(alpha = 0f)),
            center = Offset(size.width, size.height),
            radius = 250f * scale
        ),
        radius = 250f * scale,
        center = Offset(size.width, size.height)
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(primaryColor.copy(alpha = 0.5f), primaryColor.copy(alpha = 0f)),
            center = Offset(size.width * 0.7f, size.height * 0.3f),
            radius = 150f * scale
        ),
        radius = 150f * scale,
        center = Offset(size.width * 0.7f, size.height * 0.3f)
    )
}

@Composable
private fun TextElementView(
    element: TextElement,
    isSelected: Boolean,
    canvasSize: IntSize,
    padding: Float,
    onSelect: () -> Unit,
    onPositionChanged: (ElementPosition) -> Unit,
    onContentChanged: (String) -> Unit
) {
    val density = LocalDensity.current
    val style = element.style
    val position = element.position

    // Calculate actual position in pixels
    val paddingPx = with(density) { padding.dp.toPx() }
    val availableWidth = canvasSize.width - (paddingPx * 2)
    val availableHeight = canvasSize.height - (paddingPx * 2)

    val offsetX = (paddingPx + (availableWidth * position.x) - (availableWidth * position.width * position.anchorX)).roundToInt()
    val offsetY = (paddingPx + (availableHeight * position.y) - (availableHeight * 0.1f * position.anchorY)).roundToInt()

    var isDragging by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var editText by remember(element.content) { mutableStateOf(element.content) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX, offsetY) }
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    )
                } else Modifier
            )
            .padding(4.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onSelect() },
                    onDoubleTap = {
                        onSelect()
                        isEditing = true
                    }
                )
            }
            .pointerInput(isSelected) {
                if (isSelected) {
                    detectDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = { isDragging = false },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val newX = ((position.x * availableWidth + dragAmount.x) / availableWidth)
                                .coerceIn(0f, 1f)
                            val newY = ((position.y * availableHeight + dragAmount.y) / availableHeight)
                                .coerceIn(0f, 1f)
                            onPositionChanged(position.copy(x = newX, y = newY))
                        }
                    )
                }
            }
    ) {
        val textStyle = TextStyle(
            fontFamily = PoppinsFamily,
            fontSize = style.fontSize.sp,
            fontWeight = style.fontWeight.toComposeFontWeight(),
            fontStyle = if (style.fontStyle == TextFontStyle.ITALIC) FontStyle.Italic else FontStyle.Normal,
            color = Color(style.color),
            textAlign = style.textAlign.toComposeTextAlign(),
            lineHeight = (style.fontSize * style.lineHeight).sp,
            letterSpacing = style.letterSpacing.sp
        )

        // Background if set
        val backgroundModifier = if (style.backgroundColor != null) {
            Modifier
                .background(
                    color = Color(style.backgroundColor),
                    shape = RoundedCornerShape(style.backgroundCornerRadius.dp)
                )
                .padding(style.backgroundPadding.dp)
        } else {
            Modifier
        }

        Box(modifier = backgroundModifier) {
            if (isEditing && isSelected) {
                BasicTextField(
                    value = editText,
                    onValueChange = {
                        editText = it
                        onContentChanged(it)
                    },
                    textStyle = textStyle,
                    modifier = Modifier.fillMaxWidth(position.width)
                )
            } else {
                Text(
                    text = element.content,
                    style = textStyle,
                    modifier = Modifier.fillMaxWidth(position.width)
                )
            }
        }
    }
}
