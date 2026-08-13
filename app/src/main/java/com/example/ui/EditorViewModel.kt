package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.HtmlDocument
import com.example.data.HtmlDocumentRepository
import com.example.util.HtmlFormatter
import com.example.util.HtmlTemplates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

enum class EditorTab {
    CODE, SPLIT, PREVIEW
}

data class EditorUiState(
    val currentDocumentId: Long? = null,
    val documentTitle: String = "index.html",
    val content: String = HtmlTemplates.defaultHtml,
    val activeTab: EditorTab = EditorTab.CODE,
    val isSearchVisible: Boolean = false,
    val searchQuery: String = "",
    val replaceQuery: String = "",
    val searchResultsCount: Int = 0,
    val currentSearchIndex: Int = 0,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isDesktopViewport: Boolean = false,
    val toastMessage: String? = null,
    val isRecentSheetVisible: Boolean = false,
    val isTemplatesDialogVisible: Boolean = false,
    val isSaveAsDialogVisible: Boolean = false,
    val currentFileUri: String? = null,
    val isModified: Boolean = false
)

class EditorViewModel(
    private val repository: HtmlDocumentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    val recentDocuments: StateFlow<List<HtmlDocument>> = repository.allDocuments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val undoStack = java.util.ArrayDeque<String>()
    private val redoStack = java.util.ArrayDeque<String>()
    private var lastSavedContent: String = HtmlTemplates.defaultHtml

    init {
        // Create initial draft in DB if none exists
        viewModelScope.launch {
            repository.allDocuments.collect { docs ->
                if (docs.isEmpty() && _uiState.value.currentDocumentId == null) {
                    val initialDoc = HtmlDocument(
                        title = "index.html",
                        content = HtmlTemplates.defaultHtml
                    )
                    val id = repository.saveDocument(initialDoc)
                    _uiState.update { it.copy(currentDocumentId = id) }
                    lastSavedContent = HtmlTemplates.defaultHtml
                }
            }
        }
    }

    fun onContentChange(newContent: String) {
        val current = _uiState.value.content
        if (current != newContent) {
            undoStack.push(current)
            if (undoStack.size > 50) undoStack.removeLast()
            redoStack.clear()

            _uiState.update { state ->
                state.copy(
                    content = newContent,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = false,
                    isModified = newContent != lastSavedContent
                )
            }
            updateSearchResultsCount(newContent, _uiState.value.searchQuery)
        }
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.pop()
            redoStack.push(_uiState.value.content)

            _uiState.update { state ->
                state.copy(
                    content = previous,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = true,
                    isModified = previous != lastSavedContent
                )
            }
            updateSearchResultsCount(previous, _uiState.value.searchQuery)
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.pop()
            undoStack.push(_uiState.value.content)

            _uiState.update { state ->
                state.copy(
                    content = next,
                    canUndo = true,
                    canRedo = redoStack.isNotEmpty(),
                    isModified = next != lastSavedContent
                )
            }
            updateSearchResultsCount(next, _uiState.value.searchQuery)
        }
    }

    fun setActiveTab(tab: EditorTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun toggleViewportMode() {
        _uiState.update { it.copy(isDesktopViewport = !it.isDesktopViewport) }
    }

    fun toggleSearchVisible() {
        _uiState.update { it.copy(isSearchVisible = !it.isSearchVisible) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        updateSearchResultsCount(_uiState.value.content, query)
    }

    fun setReplaceQuery(query: String) {
        _uiState.update { it.copy(replaceQuery = query) }
    }

    private fun updateSearchResultsCount(content: String, query: String) {
        if (query.isEmpty()) {
            _uiState.update { it.copy(searchResultsCount = 0) }
            return
        }
        var count = 0
        var idx = 0
        while (idx != -1) {
            idx = content.indexOf(query, idx, ignoreCase = true)
            if (idx != -1) {
                count++
                idx += query.length
            }
        }
        _uiState.update { it.copy(searchResultsCount = count) }
    }

    fun replaceAll() {
        val query = _uiState.value.searchQuery
        val replace = _uiState.value.replaceQuery
        if (query.isNotEmpty()) {
            val updated = _uiState.value.content.replace(query, replace, ignoreCase = true)
            onContentChange(updated)
            showToast("すべての \"$query\" を \"$replace\" に置換しました")
        }
    }

    fun replaceNext() {
        val query = _uiState.value.searchQuery
        val replace = _uiState.value.replaceQuery
        if (query.isNotEmpty()) {
            val current = _uiState.value.content
            val idx = current.indexOf(query, 0, ignoreCase = true)
            if (idx != -1) {
                val updated = current.substring(0, idx) + replace + current.substring(idx + query.length)
                onContentChange(updated)
            }
        }
    }

    fun formatHtmlCode() {
        val formatted = HtmlFormatter.formatHtml(_uiState.value.content)
        onContentChange(formatted)
        showToast("HTMLコードを整形しました")
    }

    fun insertTag(startTag: String, endTag: String = "") {
        val current = _uiState.value.content
        val tagToInsert = if (endTag.isNotEmpty()) "$startTag$endTag" else startTag
        val updated = current + "\n" + tagToInsert
        onContentChange(updated)
    }

    fun saveDocument(titleOverride: String? = null) {
        viewModelScope.launch {
            val titleToUse = titleOverride ?: _uiState.value.documentTitle
            val doc = HtmlDocument(
                id = _uiState.value.currentDocumentId ?: 0L,
                title = titleToUse,
                content = _uiState.value.content,
                updatedAt = System.currentTimeMillis(),
                fileUri = _uiState.value.currentFileUri
            )
            val id = repository.saveDocument(doc)
            lastSavedContent = _uiState.value.content
            _uiState.update {
                it.copy(
                    currentDocumentId = id,
                    documentTitle = titleToUse,
                    isModified = false,
                    isSaveAsDialogVisible = false
                )
            }
            showToast("「$titleToUse」を保存しました")
        }
    }

    fun loadDocument(doc: HtmlDocument) {
        undoStack.clear()
        redoStack.clear()
        lastSavedContent = doc.content
        _uiState.update {
            it.copy(
                currentDocumentId = doc.id,
                documentTitle = doc.title,
                content = doc.content,
                currentFileUri = doc.fileUri,
                canUndo = false,
                canRedo = false,
                isModified = false,
                isRecentSheetVisible = false
            )
        }
    }

    fun createNewDocument(title: String = "untitled.html", code: String = HtmlTemplates.defaultHtml) {
        undoStack.clear()
        redoStack.clear()
        lastSavedContent = code
        viewModelScope.launch {
            val newDoc = HtmlDocument(
                title = title,
                content = code
            )
            val id = repository.saveDocument(newDoc)
            _uiState.update {
                it.copy(
                    currentDocumentId = id,
                    documentTitle = title,
                    content = code,
                    currentFileUri = null,
                    canUndo = false,
                    canRedo = false,
                    isModified = false,
                    isRecentSheetVisible = false,
                    isTemplatesDialogVisible = false
                )
            }
            showToast("新規ファイル作成: $title")
        }
    }

    fun deleteDocument(id: Long) {
        viewModelScope.launch {
            repository.deleteDocument(id)
            if (_uiState.value.currentDocumentId == id) {
                // Load first remaining or create new
                val docs = repository.allDocuments
                createNewDocument("untitled.html")
            }
            showToast("ファイルを削除しました")
        }
    }

    fun handleSharedCode(sharedText: String, sourceTitle: String = "共有HTML") {
        undoStack.clear()
        redoStack.clear()
        val title = if (sourceTitle.endsWith(".html")) sourceTitle else "$sourceTitle.html"
        lastSavedContent = sharedText
        viewModelScope.launch {
            val doc = HtmlDocument(
                title = title,
                content = sharedText
            )
            val id = repository.saveDocument(doc)
            _uiState.update {
                it.copy(
                    currentDocumentId = id,
                    documentTitle = title,
                    content = sharedText,
                    currentFileUri = null,
                    canUndo = false,
                    canRedo = false,
                    isModified = false
                )
            }
            showToast("共有されたコードを読み込みました")
        }
    }

    fun loadContentFromUri(context: Context, uri: Uri) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val sb = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        sb.append(line).append("\n")
                    }
                    val content = sb.toString()
                    val fileName = getFileNameFromUri(context, uri) ?: "opened_file.html"

                    undoStack.clear()
                    redoStack.clear()
                    lastSavedContent = content

                    viewModelScope.launch {
                        val doc = HtmlDocument(
                            title = fileName,
                            content = content,
                            fileUri = uri.toString()
                        )
                        val id = repository.saveDocument(doc)
                        _uiState.update {
                            it.copy(
                                currentDocumentId = id,
                                documentTitle = fileName,
                                content = content,
                                currentFileUri = uri.toString(),
                                canUndo = false,
                                canRedo = false,
                                isModified = false
                            )
                        }
                        showToast("「$fileName」を開きました")
                    }
                }
            }
        } catch (e: Exception) {
            showToast("ファイルの読み込みに失敗しました: ${e.localizedMessage}")
        }
    }

    fun exportToFileUri(context: Context, uri: Uri) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(_uiState.value.content)
                }
            }
            val fileName = getFileNameFromUri(context, uri) ?: _uiState.value.documentTitle
            _uiState.update {
                it.copy(
                    documentTitle = fileName,
                    currentFileUri = uri.toString(),
                    isModified = false
                )
            }
            lastSavedContent = _uiState.value.content
            saveDocument(fileName)
            showToast("端末に「$fileName」を保存しました")
        } catch (e: Exception) {
            showToast("ファイルの保存に失敗しました: ${e.localizedMessage}")
        }
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var name: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    name = it.getString(displayNameIndex)
                }
            }
        }
        return name
    }

    fun showToast(msg: String) {
        _uiState.update { it.copy(toastMessage = msg) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun setRecentSheetVisible(visible: Boolean) {
        _uiState.update { it.copy(isRecentSheetVisible = visible) }
    }

    fun setTemplatesDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(isTemplatesDialogVisible = visible) }
    }

    fun setSaveAsDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(isSaveAsDialogVisible = visible) }
    }
}

class EditorViewModelFactory(
    private val repository: HtmlDocumentRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditorViewModel::class.java)) {
            return EditorViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
