package com.quotey.create.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.quotey.create.data.model.*
import com.quotey.create.ui.theme.PoppinsFamily
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

@Composable
fun QuoteyCanvas(
    page: QuoteyPage,
    selectedTextElementId: String?,
    selectedShapeElementId: String?,
    selectedImageElementId: String?,
    onTextElementSelected: (String?) -> Unit,
    onShapeElementSelected: (String?) -> Unit,
    onImageElementSelected: (String?) -> Unit,
    onTextPositionChanged: (String, ElementPosition) -> Unit,
    onShapePositionChanged: (String, ElementPosition) -> Unit,
    onImagePositionChanged: (String, ElementPosition) -> Unit,
    onTextContentChanged: (String, String) -> Unit,
    onBackgroundTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canvas = page.canvas
    val background = page.background
    val density = LocalDensity.current

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(canvas.aspectRatio.ratio)
            .shadow(
                elevation = if (canvas.shadowEnabled) canvas.shadowBlur.dp else 8.dp,
                shape = RoundedCornerShape(canvas.cornerRadius.dp),
                spotColor = Color(canvas.shadowColor.toULong())
            )
            .clip(RoundedCornerShape(canvas.cornerRadius.dp))
            .onSizeChanged { canvasSize = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        // Check if tap is on any element
                        val tapOnTextElement = page.textElements.any { element ->
                            isPointInElement(offset, element.position, canvasSize, canvas.padding)
                        }
                        val tapOnShapeElement = page.shapeElements.any { element ->
                            isPointInElement(offset, element.position, canvasSize, canvas.padding)
                        }
                        val tapOnImageElement = page.imageElements.any { element ->
                            isPointInElement(offset, element.position, canvasSize, canvas.padding)
                        }
                        if (!tapOnTextElement && !tapOnShapeElement && !tapOnImageElement) {
                            onBackgroundTap()
                        }
                    }
                )
            }
    ) {
        // Background layer
        BackgroundLayer(
            background = background,
            cornerRadius = canvas.cornerRadius,
            modifier = Modifier.fillMaxSize()
        )

        // Render all elements in layer order
        val allElements = page.allElements

        allElements.forEach { canvasElement ->
            when (canvasElement) {
                is CanvasElement.Text -> {
                    TextElementView(
                        element = canvasElement.element,
                        isSelected = canvasElement.element.id == selectedTextElementId,
                        canvasSize = canvasSize,
                        padding = canvas.padding,
                        onSelect = { onTextElementSelected(canvasElement.element.id) },
                        onPositionChanged = { position ->
                            onTextPositionChanged(canvasElement.element.id, position)
                        },
                        onContentChanged = { content ->
                            onTextContentChanged(canvasElement.element.id, content)
                        }
                    )
                }
                is CanvasElement.Shape -> {
                    ShapeElementView(
                        element = canvasElement.element,
                        isSelected = canvasElement.element.id == selectedShapeElementId,
                        canvasSize = canvasSize,
                        padding = canvas.padding,
                        onSelect = { onShapeElementSelected(canvasElement.element.id) },
                        onPositionChanged = { position ->
                            onShapePositionChanged(canvasElement.element.id, position)
                        }
                    )
                }
                is CanvasElement.Image -> {
                    ImageElementView(
                        element = canvasElement.element,
                        isSelected = canvasElement.element.id == selectedImageElementId,
                        canvasSize = canvasSize,
                        padding = canvas.padding,
                        onSelect = { onImageElementSelected(canvasElement.element.id) },
                        onPositionChanged = { position ->
                            onImagePositionChanged(canvasElement.element.id, position)
                        }
                    )
                }
            }
        }
    }
}

private fun isPointInElement(
    point: Offset,
    position: ElementPosition,
    canvasSize: IntSize,
    padding: Float
): Boolean {
    val paddingPx = padding
    val availableWidth = canvasSize.width - (paddingPx * 2)
    val availableHeight = canvasSize.height - (paddingPx * 2)

    val elementX = paddingPx + (availableWidth * position.x) - (availableWidth * position.width * position.anchorX)
    val elementY = paddingPx + (availableHeight * position.y) - (availableHeight * 0.1f * position.anchorY)
    val elementWidth = availableWidth * position.width
    val elementHeight = availableHeight * 0.2f // Approximate height

    return point.x >= elementX && point.x <= elementX + elementWidth &&
            point.y >= elementY && point.y <= elementY + elementHeight
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
                    color = Color(background.solidColor.toULong()),
                    shape = RoundedCornerShape(cornerRadius.dp)
                )
            )
        }

        BackgroundType.GRADIENT -> {
            val gradient = background.gradient
            // Ensure we have at least 2 valid colors for gradients, with safe fallback
            // Also deduplicate adjacent identical colors to prevent rendering issues
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

            Box(modifier = modifier) {
                Canvas(modifier = Modifier.fillMaxSize()) {
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
                                    start = Offset(
                                        centerX - len * cos(angleRad),
                                        centerY - len * sin(angleRad)
                                    ),
                                    end = Offset(
                                        centerX + len * cos(angleRad),
                                        centerY + len * sin(angleRad)
                                    )
                                )
                            }
                            GradientType.RADIAL -> {
                                val radius = (gradient.radius * maxOf(size.width, size.height)).coerceAtLeast(1f)
                                Brush.radialGradient(
                                    colors = safeColors,
                                    center = Offset(
                                        (gradient.centerX * size.width).coerceIn(0f, size.width),
                                        (gradient.centerY * size.height).coerceIn(0f, size.height)
                                    ),
                                    radius = radius
                                )
                            }
                            GradientType.SWEEP -> {
                                Brush.sweepGradient(
                                    colors = safeColors,
                                    center = Offset(
                                        (gradient.centerX * size.width).coerceIn(0f, size.width),
                                        (gradient.centerY * size.height).coerceIn(0f, size.height)
                                    )
                                )
                            }
                            GradientType.MESH -> {
                                // Mesh gradient approximation
                                Brush.linearGradient(colors = safeColors)
                            }
                        }
                    } catch (e: Exception) {
                        // Fallback to solid color on any gradient creation error
                        null
                    }

                    if (brush != null) {
                        try {
                            drawRect(brush = brush)
                        } catch (e: Exception) {
                            // If drawing with brush fails, fall back to solid color
                            drawRect(color = safeColors.firstOrNull() ?: Color.White)
                        }
                    } else {
                        // Fallback solid color
                        drawRect(color = safeColors.firstOrNull() ?: Color.White)
                    }
                }
            }
        }

        BackgroundType.PATTERN -> {
            Box(modifier = modifier) {
                // Base color
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(background.pattern.backgroundColor.toULong()))
                )
                // Pattern overlay
                PatternOverlay(
                    pattern = background.pattern,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        BackgroundType.IMAGE -> {
            // Fallback to solid color for now - image loading would be implemented here
            Box(
                modifier = modifier.background(
                    color = Color(background.solidColor.toULong()),
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
    val primaryColor = Color(pattern.primaryColor.toULong())
    val secondaryColor = Color(pattern.secondaryColor.toULong())

    Canvas(modifier = modifier) {
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
            PatternType.CROSS_HATCH -> {
                drawCrossHatchPattern(
                    color = primaryColor.copy(alpha = pattern.opacity),
                    scale = pattern.scale
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
            PatternType.CHEVRON -> {
                drawChevronPattern(
                    color = primaryColor.copy(alpha = pattern.opacity),
                    scale = pattern.scale
                )
            }
            PatternType.DIAMOND -> {
                drawDiamondPattern(
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
            PatternType.FRAME -> {
                drawFramePattern(
                    color = primaryColor.copy(alpha = pattern.opacity),
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

private fun DrawScope.drawCrossHatchPattern(
    color: Color,
    scale: Float
) {
    val spacing = 25f * scale
    val strokeWidth = 1f * scale
    val maxDimension = maxOf(size.width, size.height) * 2

    // First set of diagonal lines
    rotate(45f) {
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

    // Second set of diagonal lines
    rotate(-45f) {
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

private fun DrawScope.drawChevronPattern(
    color: Color,
    scale: Float
) {
    val chevronHeight = 30f * scale
    val chevronWidth = 60f * scale
    val spacing = 40f * scale
    val strokeWidth = 2f * scale

    var y = 0f
    while (y < size.height + chevronHeight) {
        var x = 0f
        while (x < size.width + chevronWidth) {
            val path = Path()
            path.moveTo(x, y + chevronHeight)
            path.lineTo(x + chevronWidth / 2, y)
            path.lineTo(x + chevronWidth, y + chevronHeight)
            drawPath(path, color, style = Stroke(width = strokeWidth))
            x += chevronWidth
        }
        y += spacing
    }
}

private fun DrawScope.drawDiamondPattern(
    color: Color,
    scale: Float
) {
    val diamondSize = 40f * scale
    val spacing = diamondSize * 1.5f
    val strokeWidth = 1f * scale

    var row = 0
    var y = 0f
    while (y < size.height + diamondSize) {
        var x = if (row % 2 == 0) 0f else spacing / 2
        while (x < size.width + diamondSize) {
            val path = Path()
            path.moveTo(x + diamondSize / 2, y)
            path.lineTo(x + diamondSize, y + diamondSize / 2)
            path.lineTo(x + diamondSize / 2, y + diamondSize)
            path.lineTo(x, y + diamondSize / 2)
            path.close()
            drawPath(path, color, style = Stroke(width = strokeWidth))
            x += spacing
        }
        y += diamondSize
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

private fun DrawScope.drawFramePattern(
    color: Color,
    scale: Float
) {
    val frameWidth = 20f * scale
    val inset = 30f * scale
    val strokeWidth = 2f * scale

    // Outer frame
    drawRect(
        color = color,
        topLeft = Offset(inset, inset),
        size = Size(size.width - inset * 2, size.height - inset * 2),
        style = Stroke(width = strokeWidth)
    )

    // Inner frame
    val innerInset = inset + frameWidth
    drawRect(
        color = color.copy(alpha = 0.5f),
        topLeft = Offset(innerInset, innerInset),
        size = Size(size.width - innerInset * 2, size.height - innerInset * 2),
        style = Stroke(width = strokeWidth * 0.5f)
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

    val elementWidth = availableWidth * position.width
    val offsetX = (paddingPx + (availableWidth * position.x) - (elementWidth * position.anchorX)).roundToInt()
    val offsetY = (paddingPx + (availableHeight * position.y) - (availableHeight * 0.1f * position.anchorY)).roundToInt()

    var isDragging by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var editText by remember(element.content) { mutableStateOf(element.content) }

    // Track drag state for smooth movement
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX + dragOffsetX.roundToInt(), offsetY + dragOffsetY.roundToInt()) }
            .graphicsLayer {
                rotationZ = position.rotation
                alpha = style.opacity
            }
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
                if (isSelected && !element.isLocked) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                            dragOffsetX = 0f
                            dragOffsetY = 0f
                        },
                        onDragEnd = {
                            isDragging = false
                            // Apply the drag offset to position
                            val newX = ((position.x * availableWidth + dragOffsetX) / availableWidth)
                                .coerceIn(0f, 1f)
                            val newY = ((position.y * availableHeight + dragOffsetY) / availableHeight)
                                .coerceIn(0f, 1f)
                            onPositionChanged(position.copy(x = newX, y = newY))
                            dragOffsetX = 0f
                            dragOffsetY = 0f
                        },
                        onDragCancel = {
                            isDragging = false
                            dragOffsetX = 0f
                            dragOffsetY = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragOffsetX += dragAmount.x
                            dragOffsetY += dragAmount.y
                        }
                    )
                }
            }
    ) {
        // Apply text transform
        val displayText = element.content.applyTransform(style.textTransform)

        val textDecoration = when (style.textDecoration) {
            TextDecorationStyle.NONE -> TextDecoration.None
            TextDecorationStyle.UNDERLINE -> TextDecoration.Underline
            TextDecorationStyle.LINE_THROUGH -> TextDecoration.LineThrough
            TextDecorationStyle.UNDERLINE_LINE_THROUGH -> TextDecoration.combine(
                listOf(TextDecoration.Underline, TextDecoration.LineThrough)
            )
        }

        val textStyle = TextStyle(
            fontFamily = PoppinsFamily,
            fontSize = style.fontSize.sp,
            fontWeight = style.fontWeight.toComposeFontWeight(),
            fontStyle = if (style.fontStyle == TextFontStyle.ITALIC) FontStyle.Italic else FontStyle.Normal,
            color = Color(style.color.toULong()),
            textAlign = style.textAlign.toComposeTextAlign(),
            lineHeight = (style.fontSize * style.lineHeight).sp,
            letterSpacing = style.letterSpacing.sp,
            textDecoration = textDecoration
        )

        // Background if set
        val backgroundModifier = if (style.backgroundColor != null) {
            Modifier
                .background(
                    color = Color(style.backgroundColor.toULong()),
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
                    text = displayText,
                    style = textStyle,
                    modifier = Modifier.fillMaxWidth(position.width)
                )
            }
        }

        // Selection handles when selected
        if (isSelected && !isEditing) {
            SelectionHandles(
                onResizeHandle = { handle, offset ->
                    // Handle resize - to be implemented
                },
                onRotationHandle = { angle ->
                    onPositionChanged(position.copy(rotation = angle))
                }
            )
        }
    }
}

@Composable
private fun SelectionHandles(
    onResizeHandle: (HandlePosition, Offset) -> Unit,
    onRotationHandle: (Float) -> Unit
) {
    val handleSize = 12.dp
    val handleColor = MaterialTheme.colorScheme.primary

    // Corner handles
    listOf(
        HandlePosition.TOP_LEFT to Alignment.TopStart,
        HandlePosition.TOP_RIGHT to Alignment.TopEnd,
        HandlePosition.BOTTOM_LEFT to Alignment.BottomStart,
        HandlePosition.BOTTOM_RIGHT to Alignment.BottomEnd
    ).forEach { (handlePos, alignment) ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Surface(
                modifier = Modifier
                    .align(alignment)
                    .offset(
                        x = when (alignment) {
                            Alignment.TopStart, Alignment.BottomStart -> (-handleSize / 2)
                            else -> (handleSize / 2)
                        },
                        y = when (alignment) {
                            Alignment.TopStart, Alignment.TopEnd -> (-handleSize / 2)
                            else -> (handleSize / 2)
                        }
                    )
                    .size(handleSize)
                    .pointerInput(handlePos) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onResizeHandle(handlePos, Offset(dragAmount.x, dragAmount.y))
                        }
                    },
                shape = CircleShape,
                color = handleColor
            ) {}
        }
    }

    // Rotation handle at top center
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-24).dp)
                .size(handleSize)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        // Calculate rotation based on drag
                        val angle = atan2(dragAmount.y, dragAmount.x) * 180f / PI.toFloat()
                        onRotationHandle(angle)
                    }
                },
            shape = CircleShape,
            color = handleColor.copy(alpha = 0.7f)
        ) {}
    }
}

@Composable
private fun ShapeElementView(
    element: ShapeElement,
    isSelected: Boolean,
    canvasSize: IntSize,
    padding: Float,
    onSelect: () -> Unit,
    onPositionChanged: (ElementPosition) -> Unit
) {
    val density = LocalDensity.current
    val style = element.style
    val position = element.position

    // Calculate actual position in pixels
    val paddingPx = with(density) { padding.dp.toPx() }
    val availableWidth = canvasSize.width - (paddingPx * 2)
    val availableHeight = canvasSize.height - (paddingPx * 2)

    val elementWidth = availableWidth * position.width
    val elementHeight = if (position.height > 0) availableHeight * position.height else elementWidth
    val offsetX = (paddingPx + (availableWidth * position.x) - (elementWidth * position.anchorX)).roundToInt()
    val offsetY = (paddingPx + (availableHeight * position.y) - (elementHeight * position.anchorY)).roundToInt()

    var isDragging by remember { mutableStateOf(false) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX + dragOffsetX.roundToInt(), offsetY + dragOffsetY.roundToInt()) }
            .size(
                width = with(density) { elementWidth.toDp() },
                height = with(density) { elementHeight.toDp() }
            )
            .graphicsLayer {
                rotationZ = position.rotation
                alpha = style.opacity
            }
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(4.dp)
                    )
                } else Modifier
            )
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onSelect() })
            }
            .pointerInput(isSelected) {
                if (isSelected && !element.isLocked) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                            dragOffsetX = 0f
                            dragOffsetY = 0f
                        },
                        onDragEnd = {
                            isDragging = false
                            val newX = ((position.x * availableWidth + dragOffsetX) / availableWidth).coerceIn(0f, 1f)
                            val newY = ((position.y * availableHeight + dragOffsetY) / availableHeight).coerceIn(0f, 1f)
                            onPositionChanged(position.copy(x = newX, y = newY))
                            dragOffsetX = 0f
                            dragOffsetY = 0f
                        },
                        onDragCancel = {
                            isDragging = false
                            dragOffsetX = 0f
                            dragOffsetY = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragOffsetX += dragAmount.x
                            dragOffsetY += dragAmount.y
                        }
                    )
                }
            }
    ) {
        // Render the shape
        Canvas(modifier = Modifier.fillMaxSize()) {
            val fillColor = style.fillColor?.let { Color(it.toULong()) }
            val strokeColor = Color(style.strokeColor.toULong())

            when (element.type) {
                ShapeType.RECTANGLE -> {
                    fillColor?.let {
                        drawRect(color = it)
                    }
                    if (style.strokeWidth > 0) {
                        drawRect(color = strokeColor, style = Stroke(width = style.strokeWidth))
                    }
                }
                ShapeType.ROUNDED_RECTANGLE -> {
                    val cornerRadius = style.cornerRadius
                    fillColor?.let {
                        drawRoundRect(
                            color = it,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
                        )
                    }
                    if (style.strokeWidth > 0) {
                        drawRoundRect(
                            color = strokeColor,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
                            style = Stroke(width = style.strokeWidth)
                        )
                    }
                }
                ShapeType.CIRCLE, ShapeType.OVAL -> {
                    val radius = minOf(size.width, size.height) / 2
                    val center = Offset(size.width / 2, size.height / 2)
                    if (element.type == ShapeType.CIRCLE) {
                        fillColor?.let {
                            drawCircle(color = it, radius = radius, center = center)
                        }
                        if (style.strokeWidth > 0) {
                            drawCircle(color = strokeColor, radius = radius, center = center, style = Stroke(width = style.strokeWidth))
                        }
                    } else {
                        fillColor?.let {
                            drawOval(color = it)
                        }
                        if (style.strokeWidth > 0) {
                            drawOval(color = strokeColor, style = Stroke(width = style.strokeWidth))
                        }
                    }
                }
                ShapeType.TRIANGLE -> {
                    val path = Path().apply {
                        moveTo(size.width / 2, 0f)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    fillColor?.let { drawPath(path, it) }
                    if (style.strokeWidth > 0) {
                        drawPath(path, strokeColor, style = Stroke(width = style.strokeWidth))
                    }
                }
                ShapeType.STAR -> {
                    val path = createStarPath(size, style.sides, style.innerRadius)
                    fillColor?.let { drawPath(path, it) }
                    if (style.strokeWidth > 0) {
                        drawPath(path, strokeColor, style = Stroke(width = style.strokeWidth))
                    }
                }
                ShapeType.HEART -> {
                    val path = createHeartPath(size)
                    fillColor?.let { drawPath(path, it) }
                    if (style.strokeWidth > 0) {
                        drawPath(path, strokeColor, style = Stroke(width = style.strokeWidth))
                    }
                }
                ShapeType.DIAMOND -> {
                    val path = Path().apply {
                        moveTo(size.width / 2, 0f)
                        lineTo(size.width, size.height / 2)
                        lineTo(size.width / 2, size.height)
                        lineTo(0f, size.height / 2)
                        close()
                    }
                    fillColor?.let { drawPath(path, it) }
                    if (style.strokeWidth > 0) {
                        drawPath(path, strokeColor, style = Stroke(width = style.strokeWidth))
                    }
                }
                ShapeType.HEXAGON -> {
                    val path = createPolygonPath(size, 6)
                    fillColor?.let { drawPath(path, it) }
                    if (style.strokeWidth > 0) {
                        drawPath(path, strokeColor, style = Stroke(width = style.strokeWidth))
                    }
                }
                ShapeType.PENTAGON -> {
                    val path = createPolygonPath(size, 5)
                    fillColor?.let { drawPath(path, it) }
                    if (style.strokeWidth > 0) {
                        drawPath(path, strokeColor, style = Stroke(width = style.strokeWidth))
                    }
                }
                ShapeType.POLYGON -> {
                    val path = createPolygonPath(size, style.sides)
                    fillColor?.let { drawPath(path, it) }
                    if (style.strokeWidth > 0) {
                        drawPath(path, strokeColor, style = Stroke(width = style.strokeWidth))
                    }
                }
                ShapeType.LINE -> {
                    drawLine(
                        color = strokeColor,
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = maxOf(style.strokeWidth, 2f)
                    )
                }
                ShapeType.ARROW -> {
                    // Arrow body
                    drawLine(
                        color = strokeColor,
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width - 20f, size.height / 2),
                        strokeWidth = maxOf(style.strokeWidth, 2f)
                    )
                    // Arrow head
                    val arrowPath = Path().apply {
                        moveTo(size.width, size.height / 2)
                        lineTo(size.width - 20f, size.height / 2 - 10f)
                        lineTo(size.width - 20f, size.height / 2 + 10f)
                        close()
                    }
                    drawPath(arrowPath, strokeColor)
                }
                ShapeType.CROSS -> {
                    val thickness = size.width * 0.3f
                    val path = Path().apply {
                        // Vertical bar
                        moveTo(size.width / 2 - thickness / 2, 0f)
                        lineTo(size.width / 2 + thickness / 2, 0f)
                        lineTo(size.width / 2 + thickness / 2, size.height)
                        lineTo(size.width / 2 - thickness / 2, size.height)
                        close()
                        // Horizontal bar
                        moveTo(0f, size.height / 2 - thickness / 2)
                        lineTo(size.width, size.height / 2 - thickness / 2)
                        lineTo(size.width, size.height / 2 + thickness / 2)
                        lineTo(0f, size.height / 2 + thickness / 2)
                        close()
                    }
                    fillColor?.let { drawPath(path, it) }
                    if (style.strokeWidth > 0) {
                        drawPath(path, strokeColor, style = Stroke(width = style.strokeWidth))
                    }
                }
                ShapeType.RING -> {
                    val outerRadius = minOf(size.width, size.height) / 2
                    val innerRadius = outerRadius * 0.6f
                    val center = Offset(size.width / 2, size.height / 2)
                    fillColor?.let {
                        drawCircle(color = it, radius = outerRadius, center = center)
                        drawCircle(color = Color.Transparent, radius = innerRadius, center = center)
                    }
                    if (style.strokeWidth > 0) {
                        drawCircle(color = strokeColor, radius = outerRadius, center = center, style = Stroke(width = style.strokeWidth))
                        drawCircle(color = strokeColor, radius = innerRadius, center = center, style = Stroke(width = style.strokeWidth))
                    }
                }
                else -> {
                    // Default rectangle for unsupported shapes
                    fillColor?.let { drawRect(color = it) }
                    if (style.strokeWidth > 0) {
                        drawRect(color = strokeColor, style = Stroke(width = style.strokeWidth))
                    }
                }
            }
        }

        // Selection handles when selected
        if (isSelected) {
            SelectionHandles(
                onResizeHandle = { _, _ -> },
                onRotationHandle = { angle ->
                    onPositionChanged(position.copy(rotation = position.rotation + angle))
                }
            )
        }
    }
}

private fun createStarPath(size: Size, points: Int, innerRadiusRatio: Float): Path {
    val path = Path()
    val outerRadius = minOf(size.width, size.height) / 2
    val innerRadius = outerRadius * innerRadiusRatio
    val centerX = size.width / 2
    val centerY = size.height / 2
    val angleStep = PI.toFloat() / points

    for (i in 0 until points * 2) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = i * angleStep - PI.toFloat() / 2
        val x = centerX + radius * cos(angle)
        val y = centerY + radius * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

private fun createHeartPath(size: Size): Path {
    val path = Path()
    val width = size.width
    val height = size.height

    path.moveTo(width / 2, height * 0.3f)

    // Left curve
    path.cubicTo(
        width * 0.1f, height * 0.1f,
        width * 0.0f, height * 0.4f,
        width / 2, height
    )

    // Right curve
    path.moveTo(width / 2, height * 0.3f)
    path.cubicTo(
        width * 0.9f, height * 0.1f,
        width * 1.0f, height * 0.4f,
        width / 2, height
    )

    return path
}

private fun createPolygonPath(size: Size, sides: Int): Path {
    val path = Path()
    val radius = minOf(size.width, size.height) / 2
    val centerX = size.width / 2
    val centerY = size.height / 2
    val angleStep = 2 * PI.toFloat() / sides

    for (i in 0 until sides) {
        val angle = i * angleStep - PI.toFloat() / 2
        val x = centerX + radius * cos(angle)
        val y = centerY + radius * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

@Composable
private fun ImageElementView(
    element: ImageElement,
    isSelected: Boolean,
    canvasSize: IntSize,
    padding: Float,
    onSelect: () -> Unit,
    onPositionChanged: (ElementPosition) -> Unit
) {
    val density = LocalDensity.current
    val style = element.style
    val position = element.position
    val context = LocalContext.current

    // Calculate actual position in pixels
    val paddingPx = with(density) { padding.dp.toPx() }
    val availableWidth = canvasSize.width - (paddingPx * 2)
    val availableHeight = canvasSize.height - (paddingPx * 2)

    val elementWidth = availableWidth * position.width
    val elementHeight = if (position.height > 0) availableHeight * position.height else elementWidth
    val offsetX = (paddingPx + (availableWidth * position.x) - (elementWidth * position.anchorX)).roundToInt()
    val offsetY = (paddingPx + (availableHeight * position.y) - (elementHeight * position.anchorY)).roundToInt()

    var isDragging by remember { mutableStateOf(false) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX + dragOffsetX.roundToInt(), offsetY + dragOffsetY.roundToInt()) }
            .size(
                width = with(density) { elementWidth.toDp() },
                height = with(density) { elementHeight.toDp() }
            )
            .graphicsLayer {
                rotationZ = position.rotation
                alpha = style.opacity
                scaleX = if (style.flipHorizontal) -1f else 1f
                scaleY = if (style.flipVertical) -1f else 1f
            }
            .clip(RoundedCornerShape(style.cornerRadius.dp))
            .then(
                if (style.borderWidth > 0) {
                    Modifier.border(
                        width = style.borderWidth.dp,
                        color = Color(style.borderColor.toULong()),
                        shape = RoundedCornerShape(style.cornerRadius.dp)
                    )
                } else Modifier
            )
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.tertiary,
                        shape = RoundedCornerShape(style.cornerRadius.dp)
                    )
                } else Modifier
            )
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onSelect() })
            }
            .pointerInput(isSelected) {
                if (isSelected && !element.isLocked) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                            dragOffsetX = 0f
                            dragOffsetY = 0f
                        },
                        onDragEnd = {
                            isDragging = false
                            val newX = ((position.x * availableWidth + dragOffsetX) / availableWidth).coerceIn(0f, 1f)
                            val newY = ((position.y * availableHeight + dragOffsetY) / availableHeight).coerceIn(0f, 1f)
                            onPositionChanged(position.copy(x = newX, y = newY))
                            dragOffsetX = 0f
                            dragOffsetY = 0f
                        },
                        onDragCancel = {
                            isDragging = false
                            dragOffsetX = 0f
                            dragOffsetY = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragOffsetX += dragAmount.x
                            dragOffsetY += dragAmount.y
                        }
                    )
                }
            }
    ) {
        // Load and display the image using Coil
        if (element.uri.isNotEmpty()) {
            AsyncImage(
                model = element.uri,
                contentDescription = "Image element",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(style.cornerRadius.dp)),
                contentScale = when (style.scaleType) {
                    ImageScaleType.FIT -> ContentScale.Fit
                    ImageScaleType.FILL -> ContentScale.FillBounds
                    ImageScaleType.CROP -> ContentScale.Crop
                    ImageScaleType.NONE -> ContentScale.None
                }
            )
        } else {
            // Placeholder
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Image",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Selection handles when selected
        if (isSelected) {
            SelectionHandles(
                onResizeHandle = { _, _ -> },
                onRotationHandle = { angle ->
                    onPositionChanged(position.copy(rotation = position.rotation + angle))
                }
            )
        }
    }
}
