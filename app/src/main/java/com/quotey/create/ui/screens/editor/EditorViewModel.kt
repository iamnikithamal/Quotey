package com.quotey.create.ui.screens.editor

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quotey.create.data.model.*
import com.quotey.create.data.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EditorEvent {
    data class ShowSnackbar(val message: String) : EditorEvent()
    data class ExportSuccess(val uri: Uri) : EditorEvent()
    data class ExportError(val message: String) : EditorEvent()
    data object ShareImage : EditorEvent()
    data object RequestImagePicker : EditorEvent()
}

enum class EditorBottomSheet {
    NONE,
    TEXT,
    BACKGROUND,
    CANVAS,
    EXPORT,
    COLOR_PICKER,
    GRADIENT_EDITOR,
    PATTERN_SELECTOR,
    FONT_SELECTOR,
    ASPECT_RATIO,
    SHAPE,
    IMAGE,
    LAYERS
}

data class EditorUiState(
    val project: QuoteyProject = QuoteyProject(),
    val selectedTextElementId: String? = null,
    val selectedShapeElementId: String? = null,
    val selectedImageElementId: String? = null,
    val activeBottomSheet: EditorBottomSheet = EditorBottomSheet.NONE,
    val isExporting: Boolean = false,
    val colorPickerTarget: ColorPickerTarget = ColorPickerTarget.NONE,
    val gradientColorIndex: Int = 0,
    val history: ProjectHistory = ProjectHistory(),
    val showGrid: Boolean = false,
    val snapToGrid: Boolean = false,
    val gridSize: Int = 20
)

enum class ColorPickerTarget {
    NONE,
    TEXT_COLOR,
    BACKGROUND_SOLID,
    GRADIENT_COLOR,
    PATTERN_PRIMARY,
    PATTERN_SECONDARY,
    PATTERN_BACKGROUND,
    TEXT_SHADOW,
    TEXT_BACKGROUND,
    TEXT_OUTLINE,
    TEXT_GLOW,
    SHAPE_FILL,
    SHAPE_STROKE,
    IMAGE_BORDER
}

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EditorEvent>()
    val events = _events.asSharedFlow()

    // Current editing state helpers
    val currentPage: QuoteyPage
        get() = _uiState.value.project.currentPage

    val currentCanvas: CanvasSettings
        get() = currentPage.canvas

    val currentBackground: BackgroundSettings
        get() = currentPage.background

    val selectedTextElement: TextElement?
        get() = currentPage.textElements.find { it.id == _uiState.value.selectedTextElementId }

    val selectedShapeElement: ShapeElement?
        get() = currentPage.shapeElements.find { it.id == _uiState.value.selectedShapeElementId }

    val selectedImageElement: ImageElement?
        get() = currentPage.imageElements.find { it.id == _uiState.value.selectedImageElementId }

    init {
        initializeProject()
    }

    private fun initializeProject() {
        val project = projectRepository.createNewProject()
        _uiState.update { it.copy(project = project) }
        saveToHistory("Initial state")
    }

    // History management
    private fun saveToHistory(description: String = "") {
        _uiState.update { state ->
            val newEntry = HistoryEntry(
                page = state.project.currentPage,
                description = description
            )
            val currentHistory = state.history
            val newEntries = if (currentHistory.currentIndex < currentHistory.entries.lastIndex) {
                currentHistory.entries.subList(0, currentHistory.currentIndex + 1) + newEntry
            } else {
                currentHistory.entries + newEntry
            }.takeLast(currentHistory.maxSize)

            state.copy(
                history = currentHistory.copy(
                    entries = newEntries,
                    currentIndex = newEntries.lastIndex
                )
            )
        }
    }

    fun undo() {
        _uiState.update { state ->
            if (!state.history.canUndo) return@update state

            val newIndex = state.history.currentIndex - 1
            // Safe bounds check
            if (newIndex < 0 || newIndex >= state.history.entries.size) return@update state
            val previousPage = state.history.entries.getOrNull(newIndex)?.page ?: return@update state
            val updatedProject = projectRepository.updateCurrentPage(state.project) { previousPage }

            state.copy(
                project = updatedProject,
                history = state.history.copy(currentIndex = newIndex)
            )
        }
    }

    fun redo() {
        _uiState.update { state ->
            if (!state.history.canRedo) return@update state

            val newIndex = state.history.currentIndex + 1
            // Safe bounds check
            if (newIndex < 0 || newIndex >= state.history.entries.size) return@update state
            val nextPage = state.history.entries.getOrNull(newIndex)?.page ?: return@update state
            val updatedProject = projectRepository.updateCurrentPage(state.project) { nextPage }

            state.copy(
                project = updatedProject,
                history = state.history.copy(currentIndex = newIndex)
            )
        }
    }

    // Bottom sheet management
    fun showBottomSheet(sheet: EditorBottomSheet) {
        _uiState.update { it.copy(activeBottomSheet = sheet) }
    }

    fun hideBottomSheet() {
        _uiState.update { it.copy(activeBottomSheet = EditorBottomSheet.NONE, colorPickerTarget = ColorPickerTarget.NONE) }
    }

    fun showColorPicker(target: ColorPickerTarget, gradientIndex: Int = 0) {
        _uiState.update {
            it.copy(
                colorPickerTarget = target,
                gradientColorIndex = gradientIndex,
                activeBottomSheet = EditorBottomSheet.COLOR_PICKER
            )
        }
    }

    // Page management
    fun setCurrentPage(index: Int) {
        _uiState.update { state ->
            state.copy(
                project = state.project.copy(currentPageIndex = index.coerceIn(0, state.project.pages.lastIndex)),
                selectedTextElementId = null,
                selectedShapeElementId = null,
                selectedImageElementId = null
            )
        }
    }

    fun addPage(copyFromCurrent: Boolean = false) {
        _uiState.update { state ->
            val updatedProject = projectRepository.addPage(state.project, copyFromCurrent)
            state.copy(project = updatedProject, selectedTextElementId = null)
        }
        saveToHistory("Add page")
    }

    fun removePage(index: Int) {
        _uiState.update { state ->
            val updatedProject = projectRepository.removePage(state.project, index)
            state.copy(project = updatedProject, selectedTextElementId = null)
        }
        saveToHistory("Remove page")
    }

    fun duplicatePage(index: Int) {
        _uiState.update { state ->
            val updatedProject = projectRepository.duplicatePage(state.project, index)
            state.copy(project = updatedProject)
        }
        saveToHistory("Duplicate page")
    }

    fun reorderPages(fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val updatedProject = projectRepository.reorderPages(state.project, fromIndex, toIndex)
            state.copy(project = updatedProject)
        }
        saveToHistory("Reorder pages")
    }

    // Element selection
    fun clearSelection() {
        _uiState.update { state ->
            state.copy(
                selectedTextElementId = null,
                selectedShapeElementId = null,
                selectedImageElementId = null
            )
        }
    }

    // Text element management
    fun selectTextElement(elementId: String?) {
        _uiState.update { state ->
            val updatedProject = projectRepository.selectTextElement(state.project, elementId)
            state.copy(
                project = updatedProject,
                selectedTextElementId = elementId,
                selectedShapeElementId = null,
                selectedImageElementId = null
            )
        }
    }

    fun addTextElement() {
        val newElement = TextElement()
        _uiState.update { state ->
            val updatedProject = projectRepository.addTextElement(state.project, newElement)
            state.copy(
                project = updatedProject,
                selectedTextElementId = newElement.id,
                selectedShapeElementId = null,
                selectedImageElementId = null
            )
        }
        saveToHistory("Add text element")
    }

    fun updateTextContent(elementId: String, content: String) {
        _uiState.update { state ->
            val updatedProject = projectRepository.updateTextElement(state.project, elementId) {
                it.copy(content = content)
            }
            state.copy(project = updatedProject)
        }
    }

    fun updateTextStyle(elementId: String, update: (TextStyle) -> TextStyle) {
        _uiState.update { state ->
            val updatedProject = projectRepository.updateTextElement(state.project, elementId) { element ->
                element.copy(style = update(element.style))
            }
            state.copy(project = updatedProject)
        }
        saveToHistory("Update text style")
    }

    fun updateTextPosition(elementId: String, position: ElementPosition) {
        _uiState.update { state ->
            val updatedProject = projectRepository.updateTextElement(state.project, elementId) { element ->
                element.copy(position = position)
            }
            state.copy(project = updatedProject)
        }
    }

    fun updateTextPositionDrag(elementId: String, position: ElementPosition) {
        // Update without saving to history (for smooth dragging)
        _uiState.update { state ->
            val updatedProject = projectRepository.updateTextElement(state.project, elementId) { element ->
                element.copy(position = position)
            }
            state.copy(project = updatedProject)
        }
    }

    fun finalizeTextPosition(elementId: String) {
        saveToHistory("Move text element")
    }

    fun removeTextElement(elementId: String) {
        _uiState.update { state ->
            val updatedProject = projectRepository.removeTextElement(state.project, elementId)
            state.copy(
                project = updatedProject,
                selectedTextElementId = if (state.selectedTextElementId == elementId) null else state.selectedTextElementId
            )
        }
        saveToHistory("Remove text element")
    }

    // Shape element management
    fun selectShapeElement(elementId: String?) {
        _uiState.update { state ->
            state.copy(
                selectedTextElementId = null,
                selectedShapeElementId = elementId,
                selectedImageElementId = null
            )
        }
    }

    fun addShapeElement(type: ShapeType = ShapeType.RECTANGLE) {
        val newElement = ShapeElement(type = type)
        _uiState.update { state ->
            val updatedPage = state.project.currentPage.copy(
                shapeElements = state.project.currentPage.shapeElements + newElement
            )
            val updatedProject = projectRepository.updateCurrentPage(state.project) { updatedPage }
            state.copy(
                project = updatedProject,
                selectedTextElementId = null,
                selectedShapeElementId = newElement.id,
                selectedImageElementId = null
            )
        }
        saveToHistory("Add shape element")
    }

    fun updateShapeStyle(elementId: String, update: (ShapeStyle) -> ShapeStyle) {
        _uiState.update { state ->
            val updatedPage = state.project.currentPage.copy(
                shapeElements = state.project.currentPage.shapeElements.map { element ->
                    if (element.id == elementId) {
                        element.copy(style = update(element.style))
                    } else element
                }
            )
            val updatedProject = projectRepository.updateCurrentPage(state.project) { updatedPage }
            state.copy(project = updatedProject)
        }
        saveToHistory("Update shape style")
    }

    fun updateShapePosition(elementId: String, position: ElementPosition) {
        _uiState.update { state ->
            val updatedPage = state.project.currentPage.copy(
                shapeElements = state.project.currentPage.shapeElements.map { element ->
                    if (element.id == elementId) {
                        element.copy(position = position)
                    } else element
                }
            )
            val updatedProject = projectRepository.updateCurrentPage(state.project) { updatedPage }
            state.copy(project = updatedProject)
        }
    }

    fun removeShapeElement(elementId: String) {
        _uiState.update { state ->
            val updatedPage = state.project.currentPage.copy(
                shapeElements = state.project.currentPage.shapeElements.filter { it.id != elementId }
            )
            val updatedProject = projectRepository.updateCurrentPage(state.project) { updatedPage }
            state.copy(
                project = updatedProject,
                selectedShapeElementId = if (state.selectedShapeElementId == elementId) null else state.selectedShapeElementId
            )
        }
        saveToHistory("Remove shape element")
    }

    // Image element management
    fun selectImageElement(elementId: String?) {
        _uiState.update { state ->
            state.copy(
                selectedTextElementId = null,
                selectedShapeElementId = null,
                selectedImageElementId = elementId
            )
        }
    }

    fun requestImagePicker() {
        viewModelScope.launch {
            _events.emit(EditorEvent.RequestImagePicker)
        }
    }

    fun addImageElement(uri: String) {
        val newElement = ImageElement(uri = uri)
        _uiState.update { state ->
            val updatedPage = state.project.currentPage.copy(
                imageElements = state.project.currentPage.imageElements + newElement
            )
            val updatedProject = projectRepository.updateCurrentPage(state.project) { updatedPage }
            state.copy(
                project = updatedProject,
                selectedTextElementId = null,
                selectedShapeElementId = null,
                selectedImageElementId = newElement.id
            )
        }
        saveToHistory("Add image element")
    }

    fun updateImageStyle(elementId: String, update: (ImageStyle) -> ImageStyle) {
        _uiState.update { state ->
            val updatedPage = state.project.currentPage.copy(
                imageElements = state.project.currentPage.imageElements.map { element ->
                    if (element.id == elementId) {
                        element.copy(style = update(element.style))
                    } else element
                }
            )
            val updatedProject = projectRepository.updateCurrentPage(state.project) { updatedPage }
            state.copy(project = updatedProject)
        }
        saveToHistory("Update image style")
    }

    fun updateImagePosition(elementId: String, position: ElementPosition) {
        _uiState.update { state ->
            val updatedPage = state.project.currentPage.copy(
                imageElements = state.project.currentPage.imageElements.map { element ->
                    if (element.id == elementId) {
                        element.copy(position = position)
                    } else element
                }
            )
            val updatedProject = projectRepository.updateCurrentPage(state.project) { updatedPage }
            state.copy(project = updatedProject)
        }
    }

    fun removeImageElement(elementId: String) {
        _uiState.update { state ->
            val updatedPage = state.project.currentPage.copy(
                imageElements = state.project.currentPage.imageElements.filter { it.id != elementId }
            )
            val updatedProject = projectRepository.updateCurrentPage(state.project) { updatedPage }
            state.copy(
                project = updatedProject,
                selectedImageElementId = if (state.selectedImageElementId == elementId) null else state.selectedImageElementId
            )
        }
        saveToHistory("Remove image element")
    }

    // Layer management
    fun bringToFront(elementId: String) {
        _uiState.update { state ->
            val currentOrder = state.project.currentPage.elementOrder.toMutableList()
            currentOrder.remove(elementId)
            currentOrder.add(elementId)

            val updatedPage = state.project.currentPage.copy(elementOrder = currentOrder)
            val updatedProject = projectRepository.updateCurrentPage(state.project) { updatedPage }
            state.copy(project = updatedProject)
        }
        saveToHistory("Bring to front")
    }

    fun sendToBack(elementId: String) {
        _uiState.update { state ->
            val currentOrder = state.project.currentPage.elementOrder.toMutableList()
            currentOrder.remove(elementId)
            currentOrder.add(0, elementId)

            val updatedPage = state.project.currentPage.copy(elementOrder = currentOrder)
            val updatedProject = projectRepository.updateCurrentPage(state.project) { updatedPage }
            state.copy(project = updatedProject)
        }
        saveToHistory("Send to back")
    }

    fun moveLayerUp(elementId: String) {
        _uiState.update { state ->
            val currentOrder = state.project.currentPage.elementOrder.toMutableList()
            val index = currentOrder.indexOf(elementId)
            if (index >= 0 && index < currentOrder.lastIndex) {
                currentOrder.removeAt(index)
                currentOrder.add(index + 1, elementId)
            }

            val updatedPage = state.project.currentPage.copy(elementOrder = currentOrder)
            val updatedProject = projectRepository.updateCurrentPage(state.project) { updatedPage }
            state.copy(project = updatedProject)
        }
        saveToHistory("Move layer up")
    }

    fun moveLayerDown(elementId: String) {
        _uiState.update { state ->
            val currentOrder = state.project.currentPage.elementOrder.toMutableList()
            val index = currentOrder.indexOf(elementId)
            if (index > 0) {
                currentOrder.removeAt(index)
                currentOrder.add(index - 1, elementId)
            }

            val updatedPage = state.project.currentPage.copy(elementOrder = currentOrder)
            val updatedProject = projectRepository.updateCurrentPage(state.project) { updatedPage }
            state.copy(project = updatedProject)
        }
        saveToHistory("Move layer down")
    }

    // Canvas settings
    fun updateCanvasSettings(update: (CanvasSettings) -> CanvasSettings) {
        _uiState.update { state ->
            val updatedProject = projectRepository.updateCanvasSettings(state.project, update)
            state.copy(project = updatedProject)
        }
        saveToHistory("Update canvas")
    }

    fun setAspectRatio(aspectRatio: AspectRatio) {
        updateCanvasSettings { it.copy(aspectRatio = aspectRatio) }
    }

    fun setCustomSize(width: Int, height: Int) {
        updateCanvasSettings {
            it.copy(
                aspectRatio = AspectRatio.CUSTOM,
                customWidth = width.coerceIn(100, 4096),
                customHeight = height.coerceIn(100, 4096)
            )
        }
    }

    fun setCornerRadius(radius: Float) {
        updateCanvasSettings { it.copy(cornerRadius = radius.coerceIn(0f, 200f)) }
    }

    fun setPadding(padding: Float) {
        updateCanvasSettings { it.copy(padding = padding.coerceIn(0f, 200f)) }
    }

    // Background settings
    fun updateBackgroundSettings(update: (BackgroundSettings) -> BackgroundSettings) {
        _uiState.update { state ->
            val updatedProject = projectRepository.updateBackgroundSettings(state.project, update)
            state.copy(project = updatedProject)
        }
        saveToHistory("Update background")
    }

    fun setBackgroundType(type: BackgroundType) {
        updateBackgroundSettings { it.copy(type = type) }
    }

    fun setSolidColor(color: Long) {
        updateBackgroundSettings { it.copy(type = BackgroundType.SOLID, solidColor = color) }
    }

    fun setGradient(gradient: GradientSettings) {
        updateBackgroundSettings { it.copy(type = BackgroundType.GRADIENT, gradient = gradient) }
    }

    fun updateGradient(update: (GradientSettings) -> GradientSettings) {
        updateBackgroundSettings { settings ->
            settings.copy(
                type = BackgroundType.GRADIENT,
                gradient = update(settings.gradient)
            )
        }
    }

    fun setGradientColors(colors: List<Long>) {
        updateGradient { it.copy(colors = colors) }
    }

    fun setGradientColor(index: Int, color: Long) {
        updateGradient { gradient ->
            val newColors = gradient.colors.toMutableList()
            if (index < newColors.size) {
                newColors[index] = color
            }
            gradient.copy(colors = newColors)
        }
    }

    fun setGradientType(type: GradientType) {
        updateGradient { it.copy(type = type) }
    }

    fun setGradientAngle(angle: Float) {
        updateGradient { it.copy(angle = angle) }
    }

    fun setPattern(pattern: PatternSettings) {
        updateBackgroundSettings { it.copy(type = BackgroundType.PATTERN, pattern = pattern) }
    }

    fun updatePattern(update: (PatternSettings) -> PatternSettings) {
        updateBackgroundSettings { settings ->
            settings.copy(
                type = BackgroundType.PATTERN,
                pattern = update(settings.pattern)
            )
        }
    }

    fun setPatternType(type: PatternType) {
        updatePattern { it.copy(type = type) }
    }

    // Color picker result handler
    fun onColorSelected(color: Long) {
        when (_uiState.value.colorPickerTarget) {
            ColorPickerTarget.TEXT_COLOR -> {
                selectedTextElement?.let { element ->
                    updateTextStyle(element.id) { it.copy(color = color) }
                }
            }
            ColorPickerTarget.BACKGROUND_SOLID -> {
                setSolidColor(color)
            }
            ColorPickerTarget.GRADIENT_COLOR -> {
                setGradientColor(_uiState.value.gradientColorIndex, color)
            }
            ColorPickerTarget.PATTERN_PRIMARY -> {
                updatePattern { it.copy(primaryColor = color) }
            }
            ColorPickerTarget.PATTERN_SECONDARY -> {
                updatePattern { it.copy(secondaryColor = color) }
            }
            ColorPickerTarget.PATTERN_BACKGROUND -> {
                updatePattern { it.copy(backgroundColor = color) }
            }
            ColorPickerTarget.TEXT_SHADOW -> {
                selectedTextElement?.let { element ->
                    updateTextStyle(element.id) { style ->
                        style.copy(
                            shadow = (style.shadow ?: TextShadowSettings()).copy(color = color)
                        )
                    }
                }
            }
            ColorPickerTarget.TEXT_BACKGROUND -> {
                selectedTextElement?.let { element ->
                    updateTextStyle(element.id) { it.copy(backgroundColor = color) }
                }
            }
            ColorPickerTarget.TEXT_OUTLINE -> {
                selectedTextElement?.let { element ->
                    updateTextStyle(element.id) { it.copy(outlineColor = color, outlineEnabled = true) }
                }
            }
            ColorPickerTarget.TEXT_GLOW -> {
                selectedTextElement?.let { element ->
                    updateTextStyle(element.id) { it.copy(glowColor = color, glowEnabled = true) }
                }
            }
            ColorPickerTarget.SHAPE_FILL -> {
                selectedShapeElement?.let { element ->
                    updateShapeStyle(element.id) { it.copy(fillColor = color) }
                }
            }
            ColorPickerTarget.SHAPE_STROKE -> {
                selectedShapeElement?.let { element ->
                    updateShapeStyle(element.id) { it.copy(strokeColor = color) }
                }
            }
            ColorPickerTarget.IMAGE_BORDER -> {
                selectedImageElement?.let { element ->
                    updateImageStyle(element.id) { it.copy(borderColor = color) }
                }
            }
            ColorPickerTarget.NONE -> {}
        }
        hideBottomSheet()
    }

    // Grid and snapping
    fun toggleGrid() {
        _uiState.update { it.copy(showGrid = !it.showGrid) }
    }

    fun toggleSnapToGrid() {
        _uiState.update { it.copy(snapToGrid = !it.snapToGrid) }
    }

    fun setGridSize(size: Int) {
        _uiState.update { it.copy(gridSize = size.coerceIn(5, 100)) }
    }

    // Export functionality
    fun exportCurrentPage(bitmap: Bitmap, settings: ExportSettings = ExportSettings()) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }

            projectRepository.saveBitmapToFile(
                bitmap = bitmap,
                settings = settings,
                pageIndex = _uiState.value.project.currentPageIndex,
                toGallery = true
            ).fold(
                onSuccess = { uri ->
                    _events.emit(EditorEvent.ExportSuccess(uri))
                    _events.emit(EditorEvent.ShowSnackbar("Image saved successfully!"))
                },
                onFailure = { error ->
                    _events.emit(EditorEvent.ExportError(error.message ?: "Export failed"))
                    _events.emit(EditorEvent.ShowSnackbar("Failed to export: ${error.message}"))
                }
            )

            _uiState.update { it.copy(isExporting = false) }
        }
    }

    fun exportAllPages(bitmaps: List<Bitmap>, settings: ExportSettings = ExportSettings()) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }

            var successCount = 0
            bitmaps.forEachIndexed { index, bitmap ->
                projectRepository.saveBitmapToFile(
                    bitmap = bitmap,
                    settings = settings,
                    pageIndex = index,
                    toGallery = true
                ).onSuccess { successCount++ }
            }

            _events.emit(EditorEvent.ShowSnackbar("Exported $successCount/${bitmaps.size} images"))
            _uiState.update { it.copy(isExporting = false) }
        }
    }
}
