package com.example.ui

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CodeEditorView
import com.example.ui.components.HtmlPreviewView
import com.example.ui.components.QuickTagToolbar
import com.example.ui.components.RecentFilesSheet
import com.example.ui.components.SaveAsDialog
import com.example.ui.components.SearchReplaceBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recentDocs by viewModel.recentDocuments.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var isMenuExpanded by remember { mutableStateOf(false) }

    // SAF Activity Launchers
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.loadContentFromUri(context, it) }
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/html")
    ) { uri ->
        uri?.let { viewModel.exportToFileUri(context, it) }
    }

    // Handle Toast
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = uiState.documentTitle + if (uiState.isModified) " *" else "",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.setRecentSheetVisible(true) }) {
                            Icon(Icons.Default.FolderOpen, contentDescription = "ファイル一覧")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.undo() },
                            enabled = uiState.canUndo
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "元に戻す")
                        }

                        IconButton(
                            onClick = { viewModel.redo() },
                            enabled = uiState.canRedo
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "やり直す")
                        }

                        IconButton(onClick = { viewModel.toggleSearchVisible() }) {
                            Icon(Icons.Default.Search, contentDescription = "検索・置換")
                        }

                        IconButton(onClick = { viewModel.saveDocument() }) {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = "保存",
                                tint = if (uiState.isModified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Share Code Action
                        IconButton(
                            onClick = {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, uiState.content)
                                    putExtra(Intent.EXTRA_TITLE, uiState.documentTitle)
                                    type = "text/html"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "HTMLコードを共有")
                                context.startActivity(shareIntent)
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "共有")
                        }

                        // Overflow Menu
                        Box {
                            IconButton(onClick = { isMenuExpanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "メニュー")
                            }

                            DropdownMenu(
                                expanded = isMenuExpanded,
                                onDismissRequest = { isMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("新規ファイル作成") },
                                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                                    onClick = {
                                        isMenuExpanded = false
                                        viewModel.createNewDocument()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("名前を付けて保存") },
                                    leadingIcon = { Icon(Icons.Default.Save, contentDescription = null) },
                                    onClick = {
                                        isMenuExpanded = false
                                        viewModel.setSaveAsDialogVisible(true)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("端末のHTMLファイルを開く") },
                                    leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null) },
                                    onClick = {
                                        isMenuExpanded = false
                                        openDocumentLauncher.launch(arrayOf("text/html", "text/plain", "*/*"))
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("端末ストレージにエクスポート") },
                                    leadingIcon = { Icon(Icons.Default.Download, contentDescription = null) },
                                    onClick = {
                                        isMenuExpanded = false
                                        createDocumentLauncher.launch(uiState.documentTitle)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("コードを整形する") },
                                    leadingIcon = { Icon(Icons.Default.AutoFixHigh, contentDescription = null) },
                                    onClick = {
                                        isMenuExpanded = false
                                        viewModel.formatHtmlCode()
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )

                // Segmented Tab Selector for Code / Split / Preview
                Surface(
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        SegmentedButton(
                            selected = uiState.activeTab == EditorTab.CODE,
                            onClick = { viewModel.setActiveTab(EditorTab.CODE) },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                            icon = { Icon(Icons.Default.Code, contentDescription = null) }
                        ) {
                            Text("コード", fontSize = 13.sp)
                        }

                        SegmentedButton(
                            selected = uiState.activeTab == EditorTab.SPLIT,
                            onClick = { viewModel.setActiveTab(EditorTab.SPLIT) },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                            icon = { Icon(Icons.Default.ViewStream, contentDescription = null) }
                        ) {
                            Text("分割", fontSize = 13.sp)
                        }

                        SegmentedButton(
                            selected = uiState.activeTab == EditorTab.PREVIEW,
                            onClick = { viewModel.setActiveTab(EditorTab.PREVIEW) },
                            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                            icon = { Icon(Icons.Default.Preview, contentDescription = null) }
                        ) {
                            Text("プレビュー", fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (uiState.activeTab == EditorTab.CODE || uiState.activeTab == EditorTab.SPLIT) {
                QuickTagToolbar(
                    onInsertTag = { start, end -> viewModel.insertTag(start, end) },
                    onFormatCode = { viewModel.formatHtmlCode() }
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search and Replace overlay
            AnimatedVisibility(
                visible = uiState.isSearchVisible,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                SearchReplaceBar(
                    searchQuery = uiState.searchQuery,
                    replaceQuery = uiState.replaceQuery,
                    resultsCount = uiState.searchResultsCount,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onReplaceQueryChange = { viewModel.setReplaceQuery(it) },
                    onReplaceNext = { viewModel.replaceNext() },
                    onReplaceAll = { viewModel.replaceAll() },
                    onClose = { viewModel.toggleSearchVisible() }
                )
            }

            // Main Editor / Preview View
            Box(modifier = Modifier.fillMaxSize()) {
                when (uiState.activeTab) {
                    EditorTab.CODE -> {
                        CodeEditorView(
                            code = uiState.content,
                            onCodeChange = { viewModel.onContentChange(it) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    EditorTab.SPLIT -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            CodeEditorView(
                                code = uiState.content,
                                onCodeChange = { viewModel.onContentChange(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                            HtmlPreviewView(
                                htmlContent = uiState.content,
                                isDesktopViewport = uiState.isDesktopViewport,
                                onToggleViewport = { viewModel.toggleViewportMode() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                        }
                    }
                    EditorTab.PREVIEW -> {
                        HtmlPreviewView(
                            htmlContent = uiState.content,
                            isDesktopViewport = uiState.isDesktopViewport,
                            onToggleViewport = { viewModel.toggleViewportMode() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    // Sheets & Dialogs
    if (uiState.isRecentSheetVisible) {
        RecentFilesSheet(
            documents = recentDocs,
            currentDocumentId = uiState.currentDocumentId,
            onSelectDocument = { viewModel.loadDocument(it) },
            onNewDocument = {
                viewModel.createNewDocument()
            },
            onOpenExternalFile = {
                viewModel.setRecentSheetVisible(false)
                openDocumentLauncher.launch(arrayOf("text/html", "text/plain", "*/*"))
            },
            onDeleteDocument = { viewModel.deleteDocument(it) },
            onDismiss = { viewModel.setRecentSheetVisible(false) }
        )
    }

    if (uiState.isSaveAsDialogVisible) {
        SaveAsDialog(
            initialTitle = uiState.documentTitle,
            onSave = { newTitle ->
                viewModel.saveDocument(newTitle)
            },
            onDismiss = { viewModel.setSaveAsDialogVisible(false) }
        )
    }
}
