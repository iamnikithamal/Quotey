package com.quotey.create.ui.screens.editor

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FormatColorFill
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.quotey.create.data.model.QuoteyPage
import com.quotey.create.ui.components.QuoteyCanvas
import com.quotey.create.ui.screens.editor.sheets.BackgroundSheet
import com.quotey.create.ui.screens.editor.sheets.CanvasSheet
import com.quotey.create.ui.screens.editor.sheets.ColorPickerSheet
import com.quotey.create.ui.screens.editor.sheets.ExportSheet
import com.quotey.create.ui.screens.editor.sheets.TextSheet
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun EditorScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is EditorEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is EditorEvent.ExportSuccess -> {
                    // Handle export success
                }
                is EditorEvent.ExportError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is EditorEvent.ShareImage -> {
                    // Handle share
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            EditorTopBar(
                onNavigateBack = onNavigateBack,
                canUndo = uiState.history.canUndo,
                canRedo = uiState.history.canRedo,
                onUndo = viewModel::undo,
                onRedo = viewModel::redo,
                onExport = { viewModel.showBottomSheet(EditorBottomSheet.EXPORT) }
            )

            // Main Canvas Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                QuoteyCanvas(
                    page = uiState.project.currentPage,
                    selectedElementId = uiState.selectedTextElementId,
                    onElementSelected = viewModel::selectTextElement,
                    onElementPositionChanged = { elementId, position ->
                        viewModel.updateTextPosition(elementId) { position }
                    },
                    onTextContentChanged = viewModel::updateTextContent,
                    onBackgroundTap = { viewModel.selectTextElement(null) }
                )
            }

            // Page thumbnails strip
            if (uiState.project.pages.size > 1 || true) { // Always show for adding pages
                PageStripView(
                    pages = uiState.project.pages,
                    currentPageIndex = uiState.project.currentPageIndex,
                    onPageSelected = viewModel::setCurrentPage,
                    onAddPage = { viewModel.addPage(false) },
                    onDuplicatePage = viewModel::duplicatePage,
                    onDeletePage = viewModel::removePage
                )
            }

            // Bottom toolbar
            EditorBottomToolbar(
                onTextClick = { viewModel.showBottomSheet(EditorBottomSheet.TEXT) },
                onBackgroundClick = { viewModel.showBottomSheet(EditorBottomSheet.BACKGROUND) },
                onCanvasClick = { viewModel.showBottomSheet(EditorBottomSheet.CANVAS) },
                onAddText = viewModel::addTextElement
            )
        }

        // Bottom sheets
        when (uiState.activeBottomSheet) {
            EditorBottomSheet.TEXT -> {
                TextSheet(
                    textElement = viewModel.selectedTextElement,
                    onDismiss = viewModel::hideBottomSheet,
                    onUpdateStyle = { update ->
                        viewModel.selectedTextElement?.let { element ->
                            viewModel.updateTextStyle(element.id, update)
                        }
                    },
                    onUpdateContent = { content ->
                        viewModel.selectedTextElement?.let { element ->
                            viewModel.updateTextContent(element.id, content)
                        }
                    },
                    onColorPickerRequest = { viewModel.showColorPicker(ColorPickerTarget.TEXT_COLOR) },
                    onDelete = {
                        viewModel.selectedTextElement?.let { element ->
                            viewModel.removeTextElement(element.id)
                        }
                        viewModel.hideBottomSheet()
                    }
                )
            }

            EditorBottomSheet.BACKGROUND -> {
                BackgroundSheet(
                    background = viewModel.currentBackground,
                    onDismiss = viewModel::hideBottomSheet,
                    onBackgroundTypeChanged = viewModel::setBackgroundType,
                    onSolidColorChanged = viewModel::setSolidColor,
                    onGradientChanged = viewModel::setGradient,
                    onPatternChanged = viewModel::setPattern,
                    onColorPickerRequest = viewModel::showColorPicker
                )
            }

            EditorBottomSheet.CANVAS -> {
                CanvasSheet(
                    canvas = viewModel.currentCanvas,
                    onDismiss = viewModel::hideBottomSheet,
                    onAspectRatioChanged = viewModel::setAspectRatio,
                    onCustomSizeChanged = viewModel::setCustomSize,
                    onCornerRadiusChanged = viewModel::setCornerRadius,
                    onPaddingChanged = viewModel::setPadding
                )
            }

            EditorBottomSheet.EXPORT -> {
                ExportSheet(
                    onDismiss = viewModel::hideBottomSheet,
                    onExport = { settings, bitmap ->
                        bitmap?.let { viewModel.exportCurrentPage(it, settings) }
                    },
                    currentPage = uiState.project.currentPage,
                    pageCount = uiState.project.pages.size,
                    isExporting = uiState.isExporting
                )
            }

            EditorBottomSheet.COLOR_PICKER -> {
                ColorPickerSheet(
                    initialColor = getInitialColorForTarget(uiState, viewModel),
                    onDismiss = viewModel::hideBottomSheet,
                    onColorSelected = viewModel::onColorSelected
                )
            }

            else -> {}
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(16.dp)
        )
    }
}

private fun getInitialColorForTarget(uiState: EditorUiState, viewModel: EditorViewModel): Long {
    return when (uiState.colorPickerTarget) {
        ColorPickerTarget.TEXT_COLOR -> viewModel.selectedTextElement?.style?.color ?: 0xFF000000
        ColorPickerTarget.BACKGROUND_SOLID -> viewModel.currentBackground.solidColor
        ColorPickerTarget.GRADIENT_COLOR -> viewModel.currentBackground.gradient.colors.firstOrNull() ?: 0xFFFFFFFF
        ColorPickerTarget.PATTERN_PRIMARY -> viewModel.currentBackground.pattern.primaryColor
        ColorPickerTarget.PATTERN_SECONDARY -> viewModel.currentBackground.pattern.secondaryColor
        ColorPickerTarget.TEXT_SHADOW -> viewModel.selectedTextElement?.style?.shadow?.color ?: 0x40000000
        ColorPickerTarget.TEXT_BACKGROUND -> viewModel.selectedTextElement?.style?.backgroundColor ?: 0x00000000
        ColorPickerTarget.NONE -> 0xFFFFFFFF
    }
}

@Composable
private fun EditorTopBar(
    onNavigateBack: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onExport: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Editor",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onUndo,
                    enabled = canUndo
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Undo,
                        contentDescription = "Undo",
                        tint = if (canUndo) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }

                IconButton(
                    onClick = onRedo,
                    enabled = canRedo
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Redo,
                        contentDescription = "Redo",
                        tint = if (canRedo) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onExport) {
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = "Export",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun PageStripView(
    pages: List<QuoteyPage>,
    currentPageIndex: Int,
    onPageSelected: (Int) -> Unit,
    onAddPage: () -> Unit,
    onDuplicatePage: (Int) -> Unit,
    onDeletePage: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            itemsIndexed(pages) { index, page ->
                PageThumbnail(
                    page = page,
                    pageIndex = index,
                    isSelected = index == currentPageIndex,
                    onClick = { onPageSelected(index) },
                    onDuplicate = { onDuplicatePage(index) },
                    onDelete = if (pages.size > 1) {{ onDeletePage(index) }} else null
                )
            }

            item {
                AddPageButton(onClick = onAddPage)
            }
        }
    }
}

@Composable
private fun PageThumbnail(
    page: QuoteyPage,
    pageIndex: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: (() -> Unit)?
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        Card(
            modifier = Modifier
                .width(60.dp)
                .aspectRatio(page.canvas.aspectRatio.ratio.coerceIn(0.5f, 2f))
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            border = if (isSelected) {
                CardDefaults.outlinedCardBorder().copy(
                    width = 2.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
                )
            } else null
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${pageIndex + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Long press menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Duplicate") },
                leadingIcon = {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = null)
                },
                onClick = {
                    onDuplicate()
                    showMenu = false
                }
            )
            if (onDelete != null) {
                DropdownMenuItem(
                    text = { Text("Delete") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Delete, contentDescription = null)
                    },
                    onClick = {
                        onDelete()
                        showMenu = false
                    }
                )
            }
        }
    }
}

@Composable
private fun AddPageButton(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(60.dp)
            .height(60.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Add page",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun EditorBottomToolbar(
    onTextClick: () -> Unit,
    onBackgroundClick: () -> Unit,
    onCanvasClick: () -> Unit,
    onAddText: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolbarButton(
                icon = Icons.Rounded.TextFields,
                label = "Text",
                onClick = onTextClick
            )
            ToolbarButton(
                icon = Icons.Rounded.FormatColorFill,
                label = "Background",
                onClick = onBackgroundClick
            )
            ToolbarButton(
                icon = Icons.Rounded.AspectRatio,
                label = "Canvas",
                onClick = onCanvasClick
            )
            ToolbarButton(
                icon = Icons.Rounded.Add,
                label = "Add Text",
                onClick = onAddText,
                isPrimary = true
            )
        }
    }
}

@Composable
private fun ToolbarButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    isPrimary: Boolean = false
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isPrimary) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceContainerHigh
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp),
                tint = if (isPrimary) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
