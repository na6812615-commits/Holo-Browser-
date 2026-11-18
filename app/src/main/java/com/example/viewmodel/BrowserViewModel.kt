package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BookmarkEntity
import com.example.data.local.DownloadEntity
import com.example.data.local.HistoryEntity
import com.example.data.local.HoloDatabase
import com.example.data.local.ShortcutEntity
import com.example.data.preferences.BrowserPreferences
import com.example.data.repository.BrowserRepository
import com.example.model.BrowserSettings
import com.example.model.BrowserTab
import com.example.model.FindInPageState
import com.example.model.SearchEngine
import com.example.model.SearchSuggestion
import com.example.model.ThemeMode
import com.example.model.UrlUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class BrowserCommand {
    data class LoadUrl(val tabId: String, val url: String) : BrowserCommand()
    data class Reload(val tabId: String) : BrowserCommand()
    data class StopLoading(val tabId: String) : BrowserCommand()
    data class GoBack(val tabId: String) : BrowserCommand()
    data class GoForward(val tabId: String) : BrowserCommand()
    data class DestroyTab(val tabId: String, val isIncognito: Boolean) : BrowserCommand()
    data class FindAll(val tabId: String, val query: String) : BrowserCommand()
    data class FindNext(val tabId: String, val forward: Boolean) : BrowserCommand()
    data class ClearFind(val tabId: String) : BrowserCommand()
    data class ClearWebData(val clearCookies: Boolean, val clearCache: Boolean) : BrowserCommand()
}

sealed class BrowserUiMessage {
    data class ShowSnackbar(val message: String, val actionLabel: String? = null, val isUndoTab: Boolean = false) : BrowserUiMessage()
}

class BrowserViewModel(application: Application) : AndroidViewModel(application) {
    private val database = HoloDatabase.getInstance(application)
    private val preferences = BrowserPreferences(application)
    private val repository = BrowserRepository(
        bookmarkDao = database.bookmarkDao(),
        historyDao = database.historyDao(),
        shortcutDao = database.shortcutDao(),
        downloadDao = database.downloadDao(),
        preferences = preferences
    )

    private val initialTab = BrowserTab()
    private val _tabs = MutableStateFlow(listOf(initialTab))
    val tabs: StateFlow<List<BrowserTab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow(initialTab.id)
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    val activeTab: StateFlow<BrowserTab> = combine(_tabs, _activeTabId) { list, id ->
        list.find { it.id == id } ?: list.first()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, initialTab)

    private val _omniboxQuery = MutableStateFlow("")
    val omniboxQuery: StateFlow<String> = _omniboxQuery.asStateFlow()

    private val _isOmniboxFocused = MutableStateFlow(false)
    val isOmniboxFocused: StateFlow<Boolean> = _isOmniboxFocused.asStateFlow()

    private val _suggestions = MutableStateFlow<List<SearchSuggestion>>(emptyList())
    val suggestions: StateFlow<List<SearchSuggestion>> = _suggestions.asStateFlow()

    private val _findInPage = MutableStateFlow(FindInPageState())
    val findInPage: StateFlow<FindInPageState> = _findInPage.asStateFlow()

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.bookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<HistoryEntity>> = repository.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shortcuts: StateFlow<List<ShortcutEntity>> = repository.shortcuts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloads: StateFlow<List<DownloadEntity>> = repository.downloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<BrowserSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BrowserSettings())

    private val _commands = MutableSharedFlow<BrowserCommand>(extraBufferCapacity = 32)
    val commands: SharedFlow<BrowserCommand> = _commands.asSharedFlow()

    private val _messages = MutableSharedFlow<BrowserUiMessage>(extraBufferCapacity = 16)
    val messages: SharedFlow<BrowserUiMessage> = _messages.asSharedFlow()

    private var lastClosedTab: BrowserTab? = null
    private var suggestionJob: Job? = null
    private val lastLoggedUrlPerTab = mutableMapOf<String, String>()

    init {
        viewModelScope.launch {
            repository.seedDefaultShortcutsIfEmpty()
        }
    }

    fun onOmniboxFocusChanged(focused: Boolean) {
        _isOmniboxFocused.value = focused
        if (focused) {
            val currentUrl = activeTab.value.url
            _omniboxQuery.value = currentUrl
            if (currentUrl.isNotBlank()) {
                fetchSuggestions(currentUrl)
            } else {
                _suggestions.value = emptyList()
            }
        } else {
            _suggestions.value = emptyList()
        }
    }

    fun onOmniboxQueryChanged(query: String) {
        _omniboxQuery.value = query
        fetchSuggestions(query)
    }

    private fun fetchSuggestions(query: String) {
        suggestionJob?.cancel()
        if (query.isBlank()) {
            _suggestions.value = emptyList()
            return
        }
        suggestionJob = viewModelScope.launch {
            delay(150)
            _suggestions.value = repository.getSuggestions(query)
        }
    }

    fun submitInput(rawInput: String, targetTabId: String = _activeTabId.value) {
        val currentSettings = settings.value
        val targetUrl = UrlUtils.normalizeInputToUrl(
            raw = rawInput,
            engine = currentSettings.searchEngine,
            httpsOnly = currentSettings.httpsOnly
        )
        if (targetUrl.isBlank()) return

        _isOmniboxFocused.value = false
        _suggestions.value = emptyList()
        _omniboxQuery.value = targetUrl
        loadUrlInTab(targetTabId, targetUrl)
    }

    fun loadUrlInTab(tabId: String, url: String) {
        val normalized = UrlUtils.normalizeInputToUrl(
            raw = url,
            engine = settings.value.searchEngine,
            httpsOnly = settings.value.httpsOnly
        )
        _tabs.update { list ->
            list.map { tab ->
                if (tab.id == tabId) {
                    tab.copy(
                        url = normalized,
                        title = UrlUtils.extractDomain(normalized).ifBlank { normalized },
                        isLoading = true,
                        progress = 10,
                        isSecure = normalized.startsWith("https://")
                    )
                } else {
                    tab
                }
            }
        }
        _commands.tryEmit(BrowserCommand.LoadUrl(tabId, normalized))
    }

    fun openNewTab(url: String = "", isIncognito: Boolean = false) {
        val normalized = if (url.isNotBlank()) {
            UrlUtils.normalizeInputToUrl(
                raw = url,
                engine = settings.value.searchEngine,
                httpsOnly = settings.value.httpsOnly
            )
        } else {
            ""
        }
        val newTab = BrowserTab(
            title = if (normalized.isBlank()) {
                if (isIncognito) "Tab ẩn danh" else "Tab mới"
            } else {
                UrlUtils.extractDomain(normalized).ifBlank { normalized }
            },
            url = normalized,
            isIncognito = isIncognito,
            isLoading = normalized.isNotBlank(),
            progress = if (normalized.isNotBlank()) 10 else 0,
            isSecure = normalized.isBlank() || normalized.startsWith("https://")
        )
        _tabs.update { it + newTab }
        _activeTabId.value = newTab.id
        _isOmniboxFocused.value = false
        if (normalized.isNotBlank()) {
            _commands.tryEmit(BrowserCommand.LoadUrl(newTab.id, normalized))
        }
    }

    fun selectTab(tabId: String) {
        if (_tabs.value.any { it.id == tabId }) {
            _activeTabId.value = tabId
            _isOmniboxFocused.value = false
            closeFindInPage()
        }
    }

    fun closeTab(tabId: String) {
        val currentList = _tabs.value
        val closingTab = currentList.find { it.id == tabId } ?: return
        lastClosedTab = closingTab
        _commands.tryEmit(BrowserCommand.DestroyTab(tabId, closingTab.isIncognito))

        val remaining = currentList.filterNot { it.id == tabId }
        if (remaining.isEmpty()) {
            val replacement = BrowserTab(isIncognito = false)
            _tabs.value = listOf(replacement)
            _activeTabId.value = replacement.id
        } else {
            _tabs.value = remaining
            if (_activeTabId.value == tabId) {
                val sameMode = remaining.lastOrNull { it.isIncognito == closingTab.isIncognito }
                _activeTabId.value = (sameMode ?: remaining.last()).id
            }
        }
        if (!closingTab.isIncognito && !closingTab.isStartPage) {
            _messages.tryEmit(
                BrowserUiMessage.ShowSnackbar(
                    message = "Đã đóng tab ${closingTab.title}",
                    actionLabel = "Hoàn tác",
                    isUndoTab = true
                )
            )
        }
    }

    fun undoCloseTab() {
        val closed = lastClosedTab ?: return
        lastClosedTab = null
        val restored = closed.copy()
        _tabs.update { it + restored }
        _activeTabId.value = restored.id
        if (restored.url.isNotBlank()) {
            _commands.tryEmit(BrowserCommand.LoadUrl(restored.id, restored.url))
        }
    }

    fun closeAllTabs(incognitoOnly: Boolean) {
        val current = _tabs.value
        val toClose = current.filter { it.isIncognito == incognitoOnly }
        toClose.forEach { tab ->
            _commands.tryEmit(BrowserCommand.DestroyTab(tab.id, tab.isIncognito))
        }
        val remaining = current.filterNot { it.isIncognito == incognitoOnly }
        if (remaining.isEmpty()) {
            val fresh = BrowserTab(isIncognito = false)
            _tabs.value = listOf(fresh)
            _activeTabId.value = fresh.id
        } else {
            _tabs.value = remaining
            if (remaining.none { it.id == _activeTabId.value }) {
                _activeTabId.value = remaining.last().id
            }
        }
    }

    fun goHome() {
        val tab = activeTab.value
        _tabs.update { list ->
            list.map {
                if (it.id == tab.id) {
                    it.copy(
                        url = "",
                        title = if (it.isIncognito) "Tab ẩn danh" else "Tab mới",
                        isLoading = false,
                        progress = 0,
                        isSecure = true
                    )
                } else it
            }
        }
        closeFindInPage()
    }

    fun goBack() {
        val tab = activeTab.value
        if (tab.canGoBack) {
            _commands.tryEmit(BrowserCommand.GoBack(tab.id))
        } else if (!tab.isStartPage) {
            goHome()
        }
    }

    fun goForward() {
        val tab = activeTab.value
        if (tab.canGoForward) {
            _commands.tryEmit(BrowserCommand.GoForward(tab.id))
        }
    }

    fun reloadOrStop() {
        val tab = activeTab.value
        if (tab.isStartPage) return
        if (tab.isLoading) {
            _commands.tryEmit(BrowserCommand.StopLoading(tab.id))
        } else {
            _commands.tryEmit(BrowserCommand.Reload(tab.id))
        }
    }

    fun toggleDesktopMode() {
        val tab = activeTab.value
        val newDesktop = !tab.isDesktopMode
        _tabs.update { list ->
            list.map {
                if (it.id == tab.id) it.copy(isDesktopMode = newDesktop) else it
            }
        }
        if (!tab.isStartPage) {
            _commands.tryEmit(BrowserCommand.Reload(tab.id))
        }
    }

    fun translateCurrentPage() {
        val tab = activeTab.value
        if (tab.url.isNotBlank()) {
            loadUrlInTab(tab.id, UrlUtils.translatePageUrl(tab.url))
        }
    }

    // WebView callbacks
    fun onWebProgressChanged(tabId: String, progress: Int) {
        _tabs.update { list ->
            list.map {
                if (it.id == tabId) {
                    it.copy(progress = progress, isLoading = progress in 1..99)
                } else it
            }
        }
    }

    fun onWebTitleReceived(tabId: String, title: String) {
        if (title.isBlank()) return
        _tabs.update { list ->
            list.map {
                if (it.id == tabId && !it.isStartPage) it.copy(title = title) else it
            }
        }
    }

    fun onWebPageStarted(tabId: String, url: String, canGoBack: Boolean, canGoForward: Boolean) {
        _tabs.update { list ->
            list.map {
                if (it.id == tabId && !it.isStartPage) {
                    it.copy(
                        url = url,
                        isLoading = true,
                        isSecure = url.startsWith("https://"),
                        canGoBack = canGoBack,
                        canGoForward = canGoForward
                    )
                } else it
            }
        }
    }

    fun onWebPageFinished(
        tabId: String,
        url: String,
        title: String,
        canGoBack: Boolean,
        canGoForward: Boolean
    ) {
        val tab = _tabs.value.find { it.id == tabId } ?: return
        if (tab.isStartPage) return
        val resolvedTitle = title.ifBlank { UrlUtils.extractDomain(url).ifBlank { url } }
        _tabs.update { list ->
            list.map {
                if (it.id == tabId) {
                    it.copy(
                        url = url,
                        title = resolvedTitle,
                        isLoading = false,
                        progress = 100,
                        isSecure = url.startsWith("https://"),
                        canGoBack = canGoBack,
                        canGoForward = canGoForward
                    )
                } else it
            }
        }
        if (!tab.isIncognito && url.startsWith("http")) {
            val prev = lastLoggedUrlPerTab[tabId]
            if (prev != url) {
                lastLoggedUrlPerTab[tabId] = url
                viewModelScope.launch {
                    repository.addHistory(resolvedTitle, url)
                }
            }
        }
    }

    // Find in page
    fun openFindInPage() {
        _findInPage.value = FindInPageState(isVisible = true)
    }

    fun updateFindQuery(query: String) {
        _findInPage.update { it.copy(query = query) }
        val tabId = _activeTabId.value
        if (query.isBlank()) {
            _findInPage.update { it.copy(activeMatchOrdinal = 0, numberOfMatches = 0) }
            _commands.tryEmit(BrowserCommand.ClearFind(tabId))
        } else {
            _commands.tryEmit(BrowserCommand.FindAll(tabId, query))
        }
    }

    fun findNext(forward: Boolean) {
        _commands.tryEmit(BrowserCommand.FindNext(_activeTabId.value, forward))
    }

    fun onFindResultReceived(activeOrdinal: Int, count: Int) {
        _findInPage.update {
            it.copy(
                activeMatchOrdinal = if (count > 0) activeOrdinal + 1 else 0,
                numberOfMatches = count
            )
        }
    }

    fun closeFindInPage() {
        if (_findInPage.value.isVisible) {
            _findInPage.value = FindInPageState()
            _commands.tryEmit(BrowserCommand.ClearFind(_activeTabId.value))
        }
    }

    // Bookmarks
    fun toggleCurrentBookmark() {
        val tab = activeTab.value
        if (tab.isStartPage) return
        viewModelScope.launch {
            val added = repository.toggleBookmark(tab.title, tab.url)
            _messages.tryEmit(
                BrowserUiMessage.ShowSnackbar(
                    if (added) "Đã lưu vào dấu trang" else "Đã xóa khỏi dấu trang"
                )
            )
        }
    }

    fun saveBookmark(title: String, url: String, folder: String) {
        viewModelScope.launch {
            repository.saveBookmark(title, url, folder)
            _messages.tryEmit(BrowserUiMessage.ShowSnackbar("Đã lưu dấu trang"))
        }
    }

    fun updateBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch { repository.updateBookmark(bookmark) }
    }

    fun deleteBookmark(id: Long) {
        viewModelScope.launch { repository.deleteBookmark(id) }
    }

    // History & Clear data
    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch { repository.deleteHistoryItem(id) }
    }

    fun clearBrowsingData(clearHistory: Boolean, clearCookies: Boolean, clearCache: Boolean) {
        viewModelScope.launch {
            if (clearHistory) repository.clearHistory()
            if (clearCookies || clearCache) {
                _commands.tryEmit(BrowserCommand.ClearWebData(clearCookies, clearCache))
            }
            _messages.tryEmit(BrowserUiMessage.ShowSnackbar("Đã xóa dữ liệu duyệt web"))
        }
    }

    // Shortcuts
    fun addShortcut(title: String, url: String) {
        if (url.isBlank()) return
        viewModelScope.launch { repository.addShortcut(title, url) }
    }

    fun deleteShortcut(id: Long) {
        viewModelScope.launch { repository.deleteShortcut(id) }
    }

    // Downloads
    fun recordDownload(fileName: String, url: String, mimeType: String, dmId: Long) {
        viewModelScope.launch {
            val rowId = repository.recordDownload(fileName, url, mimeType, dmId)
            _messages.tryEmit(BrowserUiMessage.ShowSnackbar("Đang tải xuống $fileName"))
            delay(1500)
            repository.updateDownloadStatus(rowId, "Hoàn tất / Đang xử lý")
        }
    }

    fun deleteDownload(id: Long) {
        viewModelScope.launch { repository.deleteDownload(id) }
    }

    fun clearDownloads() {
        viewModelScope.launch { repository.clearDownloads() }
    }

    // Settings
    fun setSearchEngine(engine: SearchEngine) {
        viewModelScope.launch { repository.setSearchEngine(engine) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun setJavaScriptEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setJavaScriptEnabled(enabled) }
    }

    fun setBlockPopups(blocked: Boolean) {
        viewModelScope.launch { repository.setBlockPopups(blocked) }
    }

    fun setDoNotTrack(enabled: Boolean) {
        viewModelScope.launch { repository.setDoNotTrack(enabled) }
    }

    fun setHttpsOnly(enabled: Boolean) {
        viewModelScope.launch { repository.setHttpsOnly(enabled) }
    }

    fun setTextZoom(zoom: Int) {
        viewModelScope.launch { repository.setTextZoom(zoom) }
    }

    fun showMessage(msg: String) {
        _messages.tryEmit(BrowserUiMessage.ShowSnackbar(msg))
    }
}
