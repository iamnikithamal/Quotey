package com.quotey.create.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import com.quotey.create.R
import com.quotey.create.data.model.BackgroundSettings
import com.quotey.create.data.model.BackgroundType
import com.quotey.create.data.model.CanvasSettings
import com.quotey.create.data.model.ExportFormat
import com.quotey.create.data.model.ExportSettings
import com.quotey.create.data.model.GradientType
import com.quotey.create.data.model.PatternSettings
import com.quotey.create.data.model.PatternType
import com.quotey.create.data.model.QuoteyPage
import com.quotey.create.data.model.TextAlignment
import com.quotey.create.data.model.TextElement
import com.quotey.create.data.model.TextFontStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Utility class for exporting Quotey pages to bitmap images
 */
class BitmapExporter(private val context: Context) {

    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

    /**
     * Renders a QuoteyPage to a Bitmap
     */
    fun renderPageToBitmap(
        page: QuoteyPage,
        scale: Float = 1f
    ): Bitmap {
        val canvas = page.canvas
        val width = (canvas.width * scale).toInt()
        val height = (canvas.height * scale).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val androidCanvas = Canvas(bitmap)

        // Draw background
        drawBackground(androidCanvas, page.background, width, height, canvas.cornerRadius * scale)

        // Draw text elements
        page.textElements.forEach { element ->
            drawTextElement(androidCanvas, element, canvas, scale)
        }

        // Apply corner radius mask if needed
        if (canvas.cornerRadius > 0) {
            applyCornerRadiusMask(bitmap, canvas.cornerRadius * scale)
        }

        return bitmap
    }

    private fun drawBackground(
        canvas: Canvas,
        background: BackgroundSettings,
        width: Int,
        height: Int,
        cornerRadius: Float
    ) {
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (background.type) {
            BackgroundType.SOLID -> {
                paint.color = background.solidColor.toInt()
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
            }

            BackgroundType.GRADIENT -> {
                val gradient = background.gradient
                // Ensure at least 2 colors for Android gradient constructors
                val colors = when {
                    gradient.colors.size >= 2 -> gradient.colors.map { it.toInt() }.toIntArray()
                    gradient.colors.size == 1 -> gradient.colors.getOrNull(0)?.let { color ->
                        intArrayOf(color.toInt(), color.toInt())
                    } ?: intArrayOf(Color.WHITE, Color.LTGRAY)
                    else -> intArrayOf(Color.WHITE, Color.LTGRAY)
                }

                val shader = when (gradient.type) {
                    GradientType.LINEAR -> {
                        val angleRad = gradient.angle * PI.toFloat() / 180f
                        val centerX = width / 2f
                        val centerY = height / 2f
                        val len = maxOf(width, height) / 2f

                        LinearGradient(
                            centerX - len * cos(angleRad),
                            centerY - len * sin(angleRad),
                            centerX + len * cos(angleRad),
                            centerY + len * sin(angleRad),
                            colors,
                            null,
                            Shader.TileMode.CLAMP
                        )
                    }

                    GradientType.RADIAL -> {
                        RadialGradient(
                            gradient.centerX * width,
                            gradient.centerY * height,
                            gradient.radius * maxOf(width, height),
                            colors,
                            null,
                            Shader.TileMode.CLAMP
                        )
                    }

                    GradientType.SWEEP -> {
                        SweepGradient(
                            gradient.centerX * width,
                            gradient.centerY * height,
                            colors,
                            null
                        )
                    }

                    GradientType.MESH -> {
                        // Fallback to linear for mesh
                        LinearGradient(
                            0f, 0f,
                            width.toFloat(), height.toFloat(),
                            colors,
                            null,
                            Shader.TileMode.CLAMP
                        )
                    }
                }

                paint.shader = shader
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
            }

            BackgroundType.PATTERN -> {
                // Draw white base
                paint.color = Color.WHITE
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

                // Draw pattern
                drawPattern(canvas, background.pattern, width, height)
            }

            BackgroundType.IMAGE -> {
                // Fallback to solid color for image (would need actual image loading)
                paint.color = background.solidColor.toInt()
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
            }
        }
    }

    private fun drawPattern(
        canvas: Canvas,
        pattern: PatternSettings,
        width: Int,
        height: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = pattern.primaryColor.toInt()
            alpha = (pattern.opacity * 255).toInt()
            style = Paint.Style.STROKE
            strokeWidth = 1f * pattern.scale
        }

        when (pattern.type) {
            PatternType.DOTS -> {
                paint.style = Paint.Style.FILL
                val spacing = (40f / pattern.density) * pattern.scale
                val radius = 3f * pattern.scale
                var x = spacing / 2
                while (x < width) {
                    var y = spacing / 2
                    while (y < height) {
                        canvas.drawCircle(x, y, radius, paint)
                        y += spacing
                    }
                    x += spacing
                }
            }

            PatternType.GRID -> {
                val spacing = 50f * pattern.scale
                var x = 0f
                while (x <= width) {
                    canvas.drawLine(x, 0f, x, height.toFloat(), paint)
                    x += spacing
                }
                var y = 0f
                while (y <= height) {
                    canvas.drawLine(0f, y, width.toFloat(), y, paint)
                    y += spacing
                }
            }

            PatternType.DIAGONAL_LINES -> {
                val spacing = 30f * pattern.scale
                canvas.save()
                canvas.rotate(pattern.rotation + 45f, width / 2f, height / 2f)
                val maxDim = maxOf(width, height) * 2f
                var offset = -maxDim
                while (offset < maxDim) {
                    canvas.drawLine(offset, -maxDim, offset, maxDim, paint)
                    offset += spacing
                }
                canvas.restore()
            }

            PatternType.WAVES -> {
                val amplitude = 20f * pattern.scale
                val wavelength = 80f * pattern.scale
                val spacing = 40f * pattern.scale
                paint.strokeWidth = 2f * pattern.scale

                var yOffset = 0f
                while (yOffset < height + amplitude) {
                    val path = Path()
                    path.moveTo(0f, yOffset)
                    var x = 0f
                    while (x < width) {
                        val y = yOffset + amplitude * sin(x / wavelength * 2 * PI.toFloat())
                        path.lineTo(x, y)
                        x += 2f
                    }
                    canvas.drawPath(path, paint)
                    yOffset += spacing
                }
            }

            else -> {
                // Other patterns can be added similarly
            }
        }
    }

    private fun drawTextElement(
        canvas: Canvas,
        element: TextElement,
        canvasSettings: CanvasSettings,
        scale: Float
    ) {
        val style = element.style
        val position = element.position

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = style.color.toInt()
            textSize = style.fontSize * scale
            typeface = getTypeface(style.fontFamily, style.fontWeight, style.fontStyle)
            letterSpacing = style.letterSpacing / 10f
        }

        val padding = canvasSettings.padding * scale
        val availableWidth = canvasSettings.width * scale - (padding * 2)
        val maxWidth = (availableWidth * position.width).toInt()

        val alignment = when (style.textAlign) {
            TextAlignment.LEFT -> Layout.Alignment.ALIGN_NORMAL
            TextAlignment.CENTER -> Layout.Alignment.ALIGN_CENTER
            TextAlignment.RIGHT -> Layout.Alignment.ALIGN_OPPOSITE
            TextAlignment.JUSTIFY -> Layout.Alignment.ALIGN_NORMAL
        }

        val staticLayout = StaticLayout.Builder
            .obtain(element.content, 0, element.content.length, textPaint, maxWidth)
            .setAlignment(alignment)
            .setLineSpacing(0f, style.lineHeight)
            .setIncludePad(true)
            .build()

        val x = padding + (availableWidth * position.x) - (maxWidth * position.anchorX)
        val y = padding + ((canvasSettings.height * scale - padding * 2) * position.y) -
                (staticLayout.height * position.anchorY)

        canvas.save()
        canvas.translate(x, y)

        // Draw background if set
        style.backgroundColor?.let { bgColor ->
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = bgColor.toInt()
            }
            val bgPadding = style.backgroundPadding * scale
            val bgRect = RectF(
                -bgPadding,
                -bgPadding,
                maxWidth + bgPadding,
                staticLayout.height + bgPadding
            )
            canvas.drawRoundRect(
                bgRect,
                style.backgroundCornerRadius * scale,
                style.backgroundCornerRadius * scale,
                bgPaint
            )
        }

        // Draw shadow if set
        style.shadow?.let { shadow ->
            textPaint.setShadowLayer(
                shadow.blurRadius * scale,
                shadow.offsetX * scale,
                shadow.offsetY * scale,
                shadow.color.toInt()
            )
        }

        staticLayout.draw(canvas)
        canvas.restore()
    }

    private fun getTypeface(
        fontFamily: String,
        fontWeight: Int,
        fontStyle: TextFontStyle
    ): Typeface {
        // Try to get Poppins font
        val baseTypeface = try {
            val fontRes = when {
                fontWeight <= 100 -> R.font.poppins_thin
                fontWeight <= 200 -> R.font.poppins_extralight
                fontWeight <= 300 -> R.font.poppins_light
                fontWeight <= 400 -> R.font.poppins_regular
                fontWeight <= 500 -> R.font.poppins_medium
                fontWeight <= 600 -> R.font.poppins_semibold
                fontWeight <= 700 -> R.font.poppins_bold
                fontWeight <= 800 -> R.font.poppins_extrabold
                else -> R.font.poppins_black
            }
            ResourcesCompat.getFont(context, fontRes) ?: Typeface.DEFAULT
        } catch (e: Exception) {
            Typeface.DEFAULT
        }

        return if (fontStyle == TextFontStyle.ITALIC) {
            Typeface.create(baseTypeface, Typeface.ITALIC)
        } else {
            baseTypeface
        }
    }

    private fun applyCornerRadiusMask(bitmap: Bitmap, cornerRadius: Float) {
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.TRANSPARENT
            xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR)
        }

        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()

        // Clear corners using paths
        val cornerPath = Path()

        // Top-left corner
        cornerPath.reset()
        cornerPath.moveTo(0f, 0f)
        cornerPath.lineTo(cornerRadius, 0f)
        cornerPath.arcTo(RectF(0f, 0f, cornerRadius * 2, cornerRadius * 2), -90f, -90f, false)
        cornerPath.close()
        canvas.drawPath(cornerPath, paint)

        // Top-right corner
        cornerPath.reset()
        cornerPath.moveTo(width - cornerRadius, 0f)
        cornerPath.lineTo(width, 0f)
        cornerPath.lineTo(width, cornerRadius)
        cornerPath.arcTo(
            RectF(width - cornerRadius * 2, 0f, width, cornerRadius * 2),
            0f,
            -90f,
            false
        )
        cornerPath.close()
        canvas.drawPath(cornerPath, paint)

        // Bottom-right corner
        cornerPath.reset()
        cornerPath.moveTo(width, height - cornerRadius)
        cornerPath.lineTo(width, height)
        cornerPath.lineTo(width - cornerRadius, height)
        cornerPath.arcTo(
            RectF(width - cornerRadius * 2, height - cornerRadius * 2, width, height),
            90f,
            -90f,
            false
        )
        cornerPath.close()
        canvas.drawPath(cornerPath, paint)

        // Bottom-left corner
        cornerPath.reset()
        cornerPath.moveTo(0f, height - cornerRadius)
        cornerPath.lineTo(0f, height)
        cornerPath.lineTo(cornerRadius, height)
        cornerPath.arcTo(
            RectF(0f, height - cornerRadius * 2, cornerRadius * 2, height),
            180f,
            -90f,
            false
        )
        cornerPath.close()
        canvas.drawPath(cornerPath, paint)
    }

    /**
     * Saves a bitmap to the device gallery
     */
    suspend fun saveBitmapToGallery(
        bitmap: Bitmap,
        settings: ExportSettings,
        pageIndex: Int? = null
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val timestamp = dateFormat.format(Date())
            val pageStr = pageIndex?.let { "_page${it + 1}" } ?: ""
            val fileName = "${settings.fileName}_${timestamp}${pageStr}.${settings.format.extension}"

            val compressFormat = when (settings.format) {
                ExportFormat.PNG -> Bitmap.CompressFormat.PNG
                ExportFormat.JPEG -> Bitmap.CompressFormat.JPEG
                ExportFormat.WEBP -> Bitmap.CompressFormat.WEBP_LOSSY
            }

            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use MediaStore for Android 10+
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, settings.format.mimeType)
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/Quotey"
                    )
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val imageUri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ) ?: throw Exception("Failed to create MediaStore entry")

                context.contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                    bitmap.compress(compressFormat, settings.quality, outputStream)
                }

                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(imageUri, contentValues, null, null)

                imageUri
            } else {
                // Use file system for older Android versions
                val picturesDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val quoteyDir = File(picturesDir, "Quotey")
                if (!quoteyDir.exists()) {
                    quoteyDir.mkdirs()
                }

                val file = File(quoteyDir, fileName)
                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(compressFormat, settings.quality, outputStream)
                }

                Uri.fromFile(file)
            }

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
