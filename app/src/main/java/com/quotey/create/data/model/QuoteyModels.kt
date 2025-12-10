package com.quotey.create.data.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents a complete Quotey project containing multiple pages/slides
 */
data class QuoteyProject(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Untitled",
    val pages: List<QuoteyPage> = listOf(QuoteyPage()),
    val currentPageIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val currentPage: QuoteyPage
        get() = pages.getOrElse(currentPageIndex.coerceIn(0, pages.lastIndex.coerceAtLeast(0))) {
            pages.firstOrNull() ?: QuoteyPage()
        }
}

/**
 * Represents a single page/slide in the project
 */
data class QuoteyPage(
    val id: String = UUID.randomUUID().toString(),
    val canvas: CanvasSettings = CanvasSettings(),
    val background: BackgroundSettings = BackgroundSettings(),
    val textElements: List<TextElement> = listOf(TextElement()),
    val shapeElements: List<ShapeElement> = emptyList(),
    val imageElements: List<ImageElement> = emptyList(),
    val selectedElementId: String? = null,
    val elementOrder: List<String> = emptyList() // For z-ordering
) {
    // Get all elements sorted by layer order with safe bounds checking
    val allElements: List<CanvasElement>
        get() {
            return try {
                val allItems = textElements.map { CanvasElement.Text(it) } +
                        shapeElements.map { CanvasElement.Shape(it) } +
                        imageElements.map { CanvasElement.Image(it) }
                if (elementOrder.isNotEmpty()) {
                    allItems.sortedBy { element ->
                        val index = elementOrder.indexOf(element.id)
                        if (index >= 0) index else Int.MAX_VALUE
                    }
                } else {
                    allItems
                }
            } catch (e: Exception) {
                // Fallback to unsorted list if sorting fails
                textElements.map { CanvasElement.Text(it) } +
                        shapeElements.map { CanvasElement.Shape(it) } +
                        imageElements.map { CanvasElement.Image(it) }
            }
        }
}

/**
 * Sealed class for all canvas elements
 */
sealed class CanvasElement {
    abstract val id: String
    abstract val position: ElementPosition
    abstract val isLocked: Boolean

    data class Text(val element: TextElement) : CanvasElement() {
        override val id: String get() = element.id
        override val position: ElementPosition get() = element.position
        override val isLocked: Boolean get() = element.isLocked
    }

    data class Shape(val element: ShapeElement) : CanvasElement() {
        override val id: String get() = element.id
        override val position: ElementPosition get() = element.position
        override val isLocked: Boolean get() = element.isLocked
    }

    data class Image(val element: ImageElement) : CanvasElement() {
        override val id: String get() = element.id
        override val position: ElementPosition get() = element.position
        override val isLocked: Boolean get() = element.isLocked
    }
}

/**
 * Canvas/output settings
 */
data class CanvasSettings(
    val aspectRatio: AspectRatio = AspectRatio.SQUARE,
    val customWidth: Int = 1080,
    val customHeight: Int = 1080,
    val cornerRadius: Float = 0f,
    val padding: Float = 40f,
    val shadowEnabled: Boolean = false,
    val shadowBlur: Float = 20f,
    val shadowOffsetX: Float = 0f,
    val shadowOffsetY: Float = 10f,
    val shadowColor: Long = 0x40000000
) {
    val width: Int
        get() = if (aspectRatio == AspectRatio.CUSTOM) customWidth else aspectRatio.defaultWidth

    val height: Int
        get() = if (aspectRatio == AspectRatio.CUSTOM) customHeight else aspectRatio.defaultHeight
}

/**
 * Predefined aspect ratios for social media platforms
 */
enum class AspectRatio(
    val displayName: String,
    val ratioWidth: Int,
    val ratioHeight: Int,
    val defaultWidth: Int,
    val defaultHeight: Int
) {
    SQUARE("Square (1:1)", 1, 1, 1080, 1080),
    PORTRAIT("Portrait (4:5)", 4, 5, 1080, 1350),
    LANDSCAPE("Landscape (16:9)", 16, 9, 1920, 1080),
    STORY("Story (9:16)", 9, 16, 1080, 1920),
    TWITTER("Twitter (16:9)", 16, 9, 1600, 900),
    FACEBOOK("Facebook (1.91:1)", 191, 100, 1200, 628),
    PINTEREST("Pinterest (2:3)", 2, 3, 1000, 1500),
    LINKEDIN("LinkedIn (1.91:1)", 191, 100, 1200, 628),
    YOUTUBE_THUMB("YouTube (16:9)", 16, 9, 1280, 720),
    CUSTOM("Custom", 1, 1, 1080, 1080);

    val ratio: Float
        get() = ratioWidth.toFloat() / ratioHeight.toFloat()
}

/**
 * Background configuration
 */
data class BackgroundSettings(
    val type: BackgroundType = BackgroundType.SOLID,
    val solidColor: Long = 0xFFFFFFFF,
    val gradient: GradientSettings = GradientSettings(),
    val pattern: PatternSettings = PatternSettings(),
    val imageUri: String? = null
)

enum class BackgroundType {
    SOLID,
    GRADIENT,
    PATTERN,
    IMAGE
}

/**
 * Gradient settings with full customization
 */
data class GradientSettings(
    val type: GradientType = GradientType.LINEAR,
    val colors: List<Long> = listOf(0xFFF5F7FA, 0xFFC3CFE2),
    val colorStops: List<Float> = listOf(0f, 1f),
    val angle: Float = 135f, // For linear gradient
    val centerX: Float = 0.5f, // For radial/sweep gradient
    val centerY: Float = 0.5f,
    val radius: Float = 1f, // For radial gradient
    val tileMode: GradientTileMode = GradientTileMode.CLAMP
)

enum class GradientType {
    LINEAR,
    RADIAL,
    SWEEP,
    MESH
}

enum class GradientTileMode {
    CLAMP,
    REPEAT,
    MIRROR
}

/**
 * Pattern/abstract background settings
 */
data class PatternSettings(
    val type: PatternType = PatternType.NONE,
    val primaryColor: Long = 0xFF8FB996,
    val secondaryColor: Long = 0x20000000,
    val scale: Float = 1f,
    val density: Float = 0.5f,
    val rotation: Float = 0f,
    val opacity: Float = 0.3f,
    val backgroundColor: Long = 0xFFFFFFFF
)

enum class PatternType {
    NONE,
    // Geometric patterns
    DOTS,
    GRID,
    DIAGONAL_LINES,
    CROSS_HATCH,
    HEXAGONS,
    TRIANGLES,
    CHEVRON,
    DIAMOND,
    // Soft/Organic patterns
    CIRCLES,
    WAVES,
    ORGANIC_BLOBS,
    NOISE,
    // Minimalist
    SINGLE_LINE,
    PARALLEL_LINES,
    SCATTERED_DOTS,
    CORNER_ACCENT,
    FRAME,
    // Abstract
    GRADIENT_MESH,
    PAINTERLY_BLUR,
    SOFT_SHAPES
}

/**
 * Text element with full styling options
 */
data class TextElement(
    val id: String = UUID.randomUUID().toString(),
    val content: String = "Your text here",
    val style: TextStyle = TextStyle(),
    val position: ElementPosition = ElementPosition(),
    val isSelected: Boolean = false,
    val isLocked: Boolean = false
)

/**
 * Shape element for geometric shapes
 */
data class ShapeElement(
    val id: String = UUID.randomUUID().toString(),
    val type: ShapeType = ShapeType.RECTANGLE,
    val position: ElementPosition = ElementPosition(width = 0.3f),
    val style: ShapeStyle = ShapeStyle(),
    val isSelected: Boolean = false,
    val isLocked: Boolean = false
)

enum class ShapeType {
    RECTANGLE,
    ROUNDED_RECTANGLE,
    CIRCLE,
    OVAL,
    TRIANGLE,
    STAR,
    POLYGON,
    LINE,
    ARROW,
    HEART,
    DIAMOND,
    HEXAGON,
    PENTAGON,
    CROSS,
    RING,
    ARC,
    CUSTOM_PATH
}

/**
 * Shape styling options
 */
data class ShapeStyle(
    val fillColor: Long? = 0xFF8FB996,
    val strokeColor: Long = 0xFF000000,
    val strokeWidth: Float = 0f,
    val cornerRadius: Float = 0f,
    val opacity: Float = 1f,
    val shadowEnabled: Boolean = false,
    val shadowColor: Long = 0x40000000,
    val shadowBlur: Float = 10f,
    val shadowOffsetX: Float = 0f,
    val shadowOffsetY: Float = 5f,
    // For polygon/star
    val sides: Int = 5,
    val innerRadius: Float = 0.5f, // For star shape
    // Gradient fill
    val useGradientFill: Boolean = false,
    val gradientSettings: GradientSettings = GradientSettings()
)

/**
 * Image element for imported images
 */
data class ImageElement(
    val id: String = UUID.randomUUID().toString(),
    val uri: String = "",
    val position: ElementPosition = ElementPosition(width = 0.5f),
    val style: ImageStyle = ImageStyle(),
    val isSelected: Boolean = false,
    val isLocked: Boolean = false
)

/**
 * Image styling options
 */
data class ImageStyle(
    val cornerRadius: Float = 0f,
    val opacity: Float = 1f,
    val borderWidth: Float = 0f,
    val borderColor: Long = 0xFF000000,
    val shadowEnabled: Boolean = false,
    val shadowColor: Long = 0x40000000,
    val shadowBlur: Float = 10f,
    val shadowOffsetX: Float = 0f,
    val shadowOffsetY: Float = 5f,
    val scaleType: ImageScaleType = ImageScaleType.FIT,
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    // Filters
    val brightness: Float = 1f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val blur: Float = 0f
)

enum class ImageScaleType {
    FIT,
    FILL,
    CROP,
    NONE
}

/**
 * Comprehensive text styling
 */
data class TextStyle(
    val fontFamily: String = "Poppins",
    val fontSize: Float = 32f,
    val fontWeight: Int = 400, // 100-900
    val fontStyle: TextFontStyle = TextFontStyle.NORMAL,
    val color: Long = 0xFF1C1B1B,
    val textAlign: TextAlignment = TextAlignment.CENTER,
    val lineHeight: Float = 1.4f,
    val letterSpacing: Float = 0f,
    val textDecoration: TextDecorationStyle = TextDecorationStyle.NONE,
    val shadow: TextShadowSettings? = null,
    val backgroundColor: Long? = null,
    val backgroundPadding: Float = 8f,
    val backgroundCornerRadius: Float = 4f,
    // Advanced effects
    val outlineEnabled: Boolean = false,
    val outlineColor: Long = 0xFF000000,
    val outlineWidth: Float = 2f,
    val glowEnabled: Boolean = false,
    val glowColor: Long = 0xFFFFFFFF,
    val glowRadius: Float = 10f,
    val useGradientFill: Boolean = false,
    val gradientColors: List<Long> = listOf(0xFF000000, 0xFF000000),
    val textTransform: TextTransform = TextTransform.NONE,
    val opacity: Float = 1f
)

enum class TextTransform {
    NONE,
    UPPERCASE,
    LOWERCASE,
    CAPITALIZE
}

enum class TextFontStyle {
    NORMAL,
    ITALIC
}

enum class TextAlignment {
    LEFT,
    CENTER,
    RIGHT,
    JUSTIFY
}

enum class TextDecorationStyle {
    NONE,
    UNDERLINE,
    LINE_THROUGH,
    UNDERLINE_LINE_THROUGH
}

/**
 * Text shadow settings
 */
data class TextShadowSettings(
    val color: Long = 0x40000000,
    val offsetX: Float = 2f,
    val offsetY: Float = 2f,
    val blurRadius: Float = 4f
)

/**
 * Element position and sizing with improved control
 */
data class ElementPosition(
    val x: Float = 0.5f, // 0-1 relative to canvas
    val y: Float = 0.5f,
    val width: Float = 0.9f, // 0-1 relative to canvas
    val height: Float = 0f, // 0 means auto-calculated based on content
    val rotation: Float = 0f,
    val anchorX: Float = 0.5f, // Anchor point 0-1
    val anchorY: Float = 0.5f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f
)

/**
 * Export settings
 */
data class ExportSettings(
    val format: ExportFormat = ExportFormat.PNG,
    val quality: Int = 100, // 0-100, for JPEG
    val scale: Float = 1f, // Multiplier for resolution
    val includeAllPages: Boolean = true,
    val fileName: String = "quotey_export",
    val transparentBackground: Boolean = false,
    val watermarkEnabled: Boolean = false,
    val watermarkText: String = ""
)

enum class ExportFormat(val extension: String, val mimeType: String) {
    PNG("png", "image/png"),
    JPEG("jpg", "image/jpeg"),
    WEBP("webp", "image/webp")
}

/**
 * App preferences
 */
@Serializable
data class AppPreferences(
    val hasCompletedOnboarding: Boolean = false,
    val defaultAspectRatio: String = AspectRatio.SQUARE.name,
    val defaultExportFormat: String = ExportFormat.PNG.name,
    val defaultExportQuality: Int = 100,
    val autoSaveEnabled: Boolean = true,
    val themeMode: String = ThemeMode.SYSTEM.name,
    val showGridLines: Boolean = false,
    val snapToGrid: Boolean = false,
    val gridSize: Int = 20
)

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

/**
 * History entry for undo/redo
 */
data class HistoryEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val page: QuoteyPage,
    val description: String = ""
)

/**
 * Project history for undo/redo functionality
 */
data class ProjectHistory(
    val entries: List<HistoryEntry> = emptyList(),
    val currentIndex: Int = -1,
    val maxSize: Int = 50
) {
    val canUndo: Boolean
        get() = currentIndex > 0

    val canRedo: Boolean
        get() = currentIndex < entries.lastIndex
}

/**
 * Handle position for resize/rotation
 */
enum class HandlePosition {
    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT,
    MIDDLE_LEFT,
    MIDDLE_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_CENTER,
    BOTTOM_RIGHT,
    ROTATION
}

/**
 * Element selection state for interactive editing
 */
data class ElementSelectionState(
    val elementId: String? = null,
    val activeHandle: HandlePosition? = null,
    val isDragging: Boolean = false,
    val isResizing: Boolean = false,
    val isRotating: Boolean = false,
    val dragStartPosition: Offset = Offset.Zero,
    val initialPosition: ElementPosition = ElementPosition()
)

// Extension functions for color conversion
fun Long.toComposeColor(): Color = Color(this.toULong())
fun Color.toLongColor(): Long = this.value.toLong()

// Extension for TextAlign conversion
fun TextAlignment.toComposeTextAlign(): TextAlign = when (this) {
    TextAlignment.LEFT -> TextAlign.Start
    TextAlignment.CENTER -> TextAlign.Center
    TextAlignment.RIGHT -> TextAlign.End
    TextAlignment.JUSTIFY -> TextAlign.Justify
}

// Extension for FontWeight conversion
fun Int.toComposeFontWeight(): FontWeight = when {
    this <= 100 -> FontWeight.Thin
    this <= 200 -> FontWeight.ExtraLight
    this <= 300 -> FontWeight.Light
    this <= 400 -> FontWeight.Normal
    this <= 500 -> FontWeight.Medium
    this <= 600 -> FontWeight.SemiBold
    this <= 700 -> FontWeight.Bold
    this <= 800 -> FontWeight.ExtraBold
    else -> FontWeight.Black
}

// Extension for FontStyle conversion
fun TextFontStyle.toComposeFontStyle(): FontStyle = when (this) {
    TextFontStyle.NORMAL -> FontStyle.Normal
    TextFontStyle.ITALIC -> FontStyle.Italic
}

// Apply text transform
fun String.applyTransform(transform: TextTransform): String = when (transform) {
    TextTransform.NONE -> this
    TextTransform.UPPERCASE -> this.uppercase()
    TextTransform.LOWERCASE -> this.lowercase()
    TextTransform.CAPITALIZE -> this.split(" ").joinToString(" ") {
        it.replaceFirstChar { char -> char.uppercase() }
    }
}
