package com.quotey.create.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.quotey.create.data.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

    /**
     * Creates a new project with default settings
     */
    fun createNewProject(
        name: String = "Untitled",
        aspectRatio: AspectRatio = AspectRatio.SQUARE
    ): QuoteyProject {
        return QuoteyProject(
            name = name,
            pages = listOf(
                QuoteyPage(
                    canvas = CanvasSettings(aspectRatio = aspectRatio),
                    textElements = listOf(TextElement())
                )
            )
        )
    }

    /**
     * Adds a new page to the project
     */
    fun addPage(project: QuoteyProject, copyFromCurrent: Boolean = false): QuoteyProject {
        val newPage = if (copyFromCurrent) {
            project.currentPage.copy(
                id = UUID.randomUUID().toString(),
                textElements = project.currentPage.textElements.map {
                    it.copy(id = UUID.randomUUID().toString())
                }
            )
        } else {
            QuoteyPage(
                canvas = project.currentPage.canvas.copy(),
                background = project.currentPage.background.copy()
            )
        }

        val newPages = project.pages.toMutableList().apply {
            add(project.currentPageIndex + 1, newPage)
        }

        return project.copy(
            pages = newPages,
            currentPageIndex = project.currentPageIndex + 1,
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Removes a page from the project
     */
    fun removePage(project: QuoteyProject, pageIndex: Int): QuoteyProject {
        if (project.pages.size <= 1) return project

        val newPages = project.pages.toMutableList().apply {
            removeAt(pageIndex)
        }

        val newCurrentIndex = when {
            pageIndex >= newPages.size -> newPages.lastIndex
            else -> pageIndex
        }

        return project.copy(
            pages = newPages,
            currentPageIndex = newCurrentIndex,
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Duplicates a page
     */
    fun duplicatePage(project: QuoteyProject, pageIndex: Int): QuoteyProject {
        val pageToDuplicate = project.pages.getOrNull(pageIndex) ?: return project

        val duplicatedPage = pageToDuplicate.copy(
            id = UUID.randomUUID().toString(),
            textElements = pageToDuplicate.textElements.map {
                it.copy(id = UUID.randomUUID().toString())
            }
        )

        val newPages = project.pages.toMutableList().apply {
            add(pageIndex + 1, duplicatedPage)
        }

        return project.copy(
            pages = newPages,
            currentPageIndex = pageIndex + 1,
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Reorders pages
     */
    fun reorderPages(project: QuoteyProject, fromIndex: Int, toIndex: Int): QuoteyProject {
        if (fromIndex == toIndex) return project
        if (fromIndex !in project.pages.indices || toIndex !in project.pages.indices) return project

        val newPages = project.pages.toMutableList()
        val page = newPages.removeAt(fromIndex)
        newPages.add(toIndex, page)

        val newCurrentIndex = when (project.currentPageIndex) {
            fromIndex -> toIndex
            in (fromIndex + 1)..toIndex -> project.currentPageIndex - 1
            in toIndex until fromIndex -> project.currentPageIndex + 1
            else -> project.currentPageIndex
        }

        return project.copy(
            pages = newPages,
            currentPageIndex = newCurrentIndex,
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Updates a specific page
     */
    fun updatePage(project: QuoteyProject, pageIndex: Int, update: (QuoteyPage) -> QuoteyPage): QuoteyProject {
        val updatedPages = project.pages.toMutableList()
        updatedPages.getOrNull(pageIndex)?.let { page ->
            updatedPages[pageIndex] = update(page)
        }

        return project.copy(
            pages = updatedPages,
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Updates the current page
     */
    fun updateCurrentPage(project: QuoteyProject, update: (QuoteyPage) -> QuoteyPage): QuoteyProject {
        return updatePage(project, project.currentPageIndex, update)
    }

    /**
     * Adds a text element to the current page
     */
    fun addTextElement(project: QuoteyProject, textElement: TextElement = TextElement()): QuoteyProject {
        return updateCurrentPage(project) { page ->
            page.copy(
                textElements = page.textElements + textElement,
                selectedElementId = textElement.id
            )
        }
    }

    /**
     * Updates a text element
     */
    fun updateTextElement(
        project: QuoteyProject,
        elementId: String,
        update: (TextElement) -> TextElement
    ): QuoteyProject {
        return updateCurrentPage(project) { page ->
            page.copy(
                textElements = page.textElements.map { element ->
                    if (element.id == elementId) update(element) else element
                }
            )
        }
    }

    /**
     * Removes a text element
     */
    fun removeTextElement(project: QuoteyProject, elementId: String): QuoteyProject {
        return updateCurrentPage(project) { page ->
            page.copy(
                textElements = page.textElements.filter { it.id != elementId },
                selectedElementId = if (page.selectedElementId == elementId) null else page.selectedElementId
            )
        }
    }

    /**
     * Selects a text element
     */
    fun selectTextElement(project: QuoteyProject, elementId: String?): QuoteyProject {
        return updateCurrentPage(project) { page ->
            page.copy(selectedElementId = elementId)
        }
    }

    /**
     * Updates canvas settings for current page
     */
    fun updateCanvasSettings(project: QuoteyProject, update: (CanvasSettings) -> CanvasSettings): QuoteyProject {
        return updateCurrentPage(project) { page ->
            page.copy(canvas = update(page.canvas))
        }
    }

    /**
     * Updates background settings for current page
     */
    fun updateBackgroundSettings(project: QuoteyProject, update: (BackgroundSettings) -> BackgroundSettings): QuoteyProject {
        return updateCurrentPage(project) { page ->
            page.copy(background = update(page.background))
        }
    }

    /**
     * Gets the output directory for exports
     */
    private fun getOutputDirectory(): File {
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val quoteyDir = File(picturesDir, "Quotey")
        if (!quoteyDir.exists()) {
            quoteyDir.mkdirs()
        }
        return quoteyDir
    }

    /**
     * Gets the cache directory for temporary files
     */
    private fun getCacheDirectory(): File {
        val cacheDir = File(context.cacheDir, "exports")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        return cacheDir
    }

    /**
     * Generates a filename for export
     */
    private fun generateFileName(baseName: String, format: ExportFormat, pageIndex: Int? = null): String {
        val timestamp = dateFormat.format(Date())
        val pageStr = pageIndex?.let { "_page${it + 1}" } ?: ""
        return "${baseName}_${timestamp}${pageStr}.${format.extension}"
    }

    /**
     * Saves a bitmap to file
     */
    suspend fun saveBitmapToFile(
        bitmap: Bitmap,
        settings: ExportSettings,
        pageIndex: Int? = null,
        toGallery: Boolean = true
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val fileName = generateFileName(settings.fileName, settings.format, pageIndex)
            val outputDir = if (toGallery) getOutputDirectory() else getCacheDirectory()
            val file = File(outputDir, fileName)

            FileOutputStream(file).use { outputStream ->
                val compressFormat = when (settings.format) {
                    ExportFormat.PNG -> Bitmap.CompressFormat.PNG
                    ExportFormat.JPEG -> Bitmap.CompressFormat.JPEG
                    ExportFormat.WEBP -> Bitmap.CompressFormat.WEBP_LOSSY
                }
                bitmap.compress(compressFormat, settings.quality, outputStream)
            }

            val uri = if (toGallery) {
                Uri.fromFile(file)
            } else {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            }

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cleans up old cache files
     */
    suspend fun cleanupCache() = withContext(Dispatchers.IO) {
        val cacheDir = getCacheDirectory()
        val maxAge = 24 * 60 * 60 * 1000L // 24 hours
        val now = System.currentTimeMillis()

        cacheDir.listFiles()?.forEach { file ->
            if (now - file.lastModified() > maxAge) {
                file.delete()
            }
        }
    }
}
