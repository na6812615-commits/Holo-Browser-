package com.example.ui

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import android.view.View
import android.view.ViewGroup
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.model.SuggestionSource
import com.example.model.ThemeMode
import com.example.model.WebHitContext
import com.example.ui.components.BrowserBottomBar
import com.example.ui.components.BrowserOverflowSheet
import com.example.ui.components.LongPressContextSheet
import com.example.ui.components.OmniboxSuggestionsOverlay
import com.example.ui.components.OmniboxTopBar
import com.example.ui.components.PageInfoDialog
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StartPageScreen
import com.example.ui.screens.TabSwitcherScreen
import com.example.ui.theme.HoloBrowserTheme
import com.example.ui.webview.HoloWebViewContainer
import com.example.ui.webview.WebViewPool
import com.example.viewmodel.BrowserUiMessage
import com.example.viewmodel.BrowserViewModel
import java.util.Locale

const val ROUTE_BROWSER = "browser"
const val ROUTE_TABS = "tabs"
const val ROUTE_BOOKMARKS = "bookmarks"
const val ROUTE_HISTORY = "history"
const val ROUTE_DOWNLOADS = "downloads"
const val ROUTE_SETTINGS = "settings"

@Composable
fun HoloBrowserApp(
    viewModel: BrowserViewModel
) {
    val context = LocalContext.current
    val navController = rememberNavController()

    val tabs by viewModel.tabs.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val omniboxQuery by viewModel.omniboxQuery.collectAsStateWithLifecycle()
    val isOmniboxFocused by viewModel.isOmniboxFocused.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    val findInPage by viewModel.findInPage.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val shortcuts by viewModel.shortcuts.collectAsStateWithLifecycle()
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val webViewPool = remember { WebViewPool() }
    DisposableEffect(Unit) {
        onDispose { webViewPool.destroyAll() }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel.messages) {
        viewModel.messages.collect { event ->
            when (event) {
                is BrowserUiMessage.ShowSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.actionLabel,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed && event.isUndoTab) {
                        viewModel.undoCloseTab()
                    }
                }
            }
        }
    }

    var showOverflowMenu by remember { mutableStateOf(false) }
    var showPageInfo by remember { mutableStateOf(false) }
    var longPressHit by remember { mutableStateOf<WebHitContext?>(null) }

    // File Upload Chooser State
    var pendingFileCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        pendingFileCallback?.onReceiveValue(uris.toTypedArray())
        pendingFileCallback = null
    }

    // Voice Search Launcher
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                viewModel.submitInput(spoken)
            }
        }
    }

    val launchVoiceSearch: () -> Unit = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Nói để tìm kiếm trên Holo Browser")
        }
        runCatching {
            voiceLauncher.launch(intent)
        }.onFailure {
            viewModel.showMessage("Thiết bị chưa hỗ trợ nhập liệu giọng nói")
        }
    }

    // Fullscreen HTML5 Video State
    var customVideoView by remember { mutableStateOf<View?>(null) }
    var customVideoCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

    val copyToClipboard: (String) -> Unit = { text ->
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("URL", text))
        viewModel.showMessage("Đã sao chép liên kết")
    }

    val shareText: (String) -> Unit = { text ->
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(sendIntent, "Chia sẻ liên kết"))
    }

    val isSystemDark = isSystemInDarkTheme()
    val useDarkTheme = when (settings.themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val isCurrentBookmarked = remember(bookmarks, activeTab.url) {
        activeTab.url.isNotBlank() && bookmarks.any { it.url == activeTab.url }
    }

    HoloBrowserTheme(
        darkTheme = useDarkTheme,
        isIncognito = activeTab.isIncognito
    ) {
        NavHost(
            navController = navController,
            startDestination = ROUTE_BROWSER
        ) {
            composable(ROUTE_BROWSER) {
                BackHandler(
                    enabled = customVideoView != null ||
                        isOmniboxFocused ||
                        findInPage.isVisible ||
                        activeTab.canGoBack ||
                        !activeTab.isStartPage
                ) {
                    when {
                        customVideoView != null -> {
                            customVideoCallback?.onCustomViewHidden()
                            customVideoView = null
                            customVideoCallback = null
                        }
                        isOmniboxFocused -> viewModel.onOmniboxFocusChanged(false)
                        findInPage.isVisible -> viewModel.closeFindInPage()
                        else -> viewModel.goBack()
                    }
                }

                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    topBar = {
                        OmniboxTopBar(
                            activeTab = activeTab,
                            tabCount = tabs.size,
                            searchEngine = settings.searchEngine,
                            omniboxQuery = omniboxQuery,
                            isOmniboxFocused = isOmniboxFocused,
                            findInPageState = findInPage,
                            onOmniboxClick = { viewModel.onOmniboxFocusChanged(true) },
                            onOmniboxQueryChange = viewModel::onOmniboxQueryChanged,
                            onOmniboxSubmit = { viewModel.submitInput(it) },
                            onOmniboxClose = { viewModel.onOmniboxFocusChanged(false) },
                            onSecurityClick = { showPageInfo = true },
                            onReloadOrStopClick = viewModel::reloadOrStop,
                            onVoiceSearchClick = launchVoiceSearch,
                            onTabsClick = { navController.navigate(ROUTE_TABS) },
                            onMenuClick = { showOverflowMenu = true },
                            onFindQueryChange = viewModel::updateFindQuery,
                            onFindNext = viewModel::findNext,
                            onCloseFind = viewModel::closeFindInPage
                        )
                    },
                    bottomBar = {
                        if (!isOmniboxFocused) {
                            BrowserBottomBar(
                                activeTab = activeTab,
                                isCurrentBookmarked = isCurrentBookmarked,
                                onBackClick = viewModel::goBack,
                                onForwardClick = viewModel::goForward,
                                onHomeClick = viewModel::goHome,
                                onBookmarkToggleClick = viewModel::toggleCurrentBookmark,
                                onBookmarksScreenClick = { navController.navigate(ROUTE_BOOKMARKS) }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (activeTab.isStartPage) {
                            StartPageScreen(
                                isIncognito = activeTab.isIncognito,
                                searchEngine = settings.searchEngine,
                                shortcuts = shortcuts,
                                onSearchBarClick = { viewModel.onOmniboxFocusChanged(true) },
                                onVoiceSearchClick = launchVoiceSearch,
                                onOpenUrl = { url -> viewModel.loadUrlInTab(activeTab.id, url) },
                                onSearchTopic = { topic -> viewModel.submitInput(topic) },
                                onSelectSearchEngine = viewModel::setSearchEngine,
                                onAddShortcut = viewModel::addShortcut,
                                onDeleteShortcut = viewModel::deleteShortcut
                            )
                        } else {
                            HoloWebViewContainer(
                                activeTab = activeTab,
                                settings = settings,
                                webViewPool = webViewPool,
                                commands = viewModel.commands,
                                onProgressChanged = viewModel::onWebProgressChanged,
                                onTitleReceived = viewModel::onWebTitleReceived,
                                onPageStarted = viewModel::onWebPageStarted,
                                onPageFinished = viewModel::onWebPageFinished,
                                onFindResult = viewModel::onFindResultReceived,
                                onDownloadRequested = viewModel::recordDownload,
                                onFileChooserRequested = { callback ->
                                    pendingFileCallback?.onReceiveValue(null)
                                    pendingFileCallback = callback
                                    fileChooserLauncher.launch(arrayOf("*/*"))
                                },
                                onLongPressHit = { hit -> longPressHit = hit },
                                onShowCustomView = { view, callback ->
                                    customVideoView = view
                                    customVideoCallback = callback
                                },
                                onToastMessage = viewModel::showMessage
                            )
                        }

                        if (isOmniboxFocused) {
                            OmniboxSuggestionsOverlay(
                                suggestions = suggestions,
                                onSuggestionClick = { suggestion ->
                                    val target = if (suggestion.source == SuggestionSource.WEB_SEARCH) {
                                        suggestion.text
                                    } else {
                                        suggestion.url ?: suggestion.text
                                    }
                                    viewModel.submitInput(target)
                                },
                                onInsertSuggestion = { text ->
                                    viewModel.onOmniboxQueryChanged(text)
                                },
                                onDismiss = { viewModel.onOmniboxFocusChanged(false) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            composable(ROUTE_TABS) {
                TabSwitcherScreen(
                    tabs = tabs,
                    activeTabId = activeTab.id,
                    initialIncognitoFilter = activeTab.isIncognito,
                    onSelectTab = viewModel::selectTab,
                    onCloseTab = viewModel::closeTab,
                    onNewTab = { incognito -> viewModel.openNewTab(isIncognito = incognito) },
                    onCloseAllTabs = viewModel::closeAllTabs,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(ROUTE_BOOKMARKS) {
                BookmarksScreen(
                    bookmarks = bookmarks,
                    onOpenUrl = { url -> viewModel.loadUrlInTab(activeTab.id, url) },
                    onSaveBookmark = viewModel::saveBookmark,
                    onUpdateBookmark = viewModel::updateBookmark,
                    onDeleteBookmark = viewModel::deleteBookmark,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(ROUTE_HISTORY) {
                HistoryScreen(
                    history = history,
                    onOpenUrl = { url -> viewModel.loadUrlInTab(activeTab.id, url) },
                    onDeleteItem = viewModel::deleteHistoryItem,
                    onClearBrowsingData = viewModel::clearBrowsingData,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(ROUTE_DOWNLOADS) {
                DownloadsScreen(
                    downloads = downloads,
                    onDeleteDownload = viewModel::deleteDownload,
                    onClearDownloads = viewModel::clearDownloads,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(ROUTE_SETTINGS) {
                SettingsScreen(
                    settings = settings,
                    onSelectSearchEngine = viewModel::setSearchEngine,
                    onSelectThemeMode = viewModel::setThemeMode,
                    onSetJavaScript = viewModel::setJavaScriptEnabled,
                    onSetBlockPopups = viewModel::setBlockPopups,
                    onSetDoNotTrack = viewModel::setDoNotTrack,
                    onSetHttpsOnly = viewModel::setHttpsOnly,
                    onSetTextZoom = viewModel::setTextZoom,
                    onClearBrowsingData = viewModel::clearBrowsingData,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        if (showOverflowMenu) {
            BrowserOverflowSheet(
                activeTab = activeTab,
                isBookmarked = isCurrentBookmarked,
                onDismiss = { showOverflowMenu = false },
                onForward = viewModel::goForward,
                onBookmarkToggle = viewModel::toggleCurrentBookmark,
                onReload = viewModel::reloadOrStop,
                onPageInfo = { showPageInfo = true },
                onShare = { if (activeTab.url.isNotBlank()) shareText(activeTab.url) },
                onNewTab = { viewModel.openNewTab(isIncognito = false) },
                onNewIncognitoTab = { viewModel.openNewTab(isIncognito = true) },
                onOpenBookmarks = { navController.navigate(ROUTE_BOOKMARKS) },
                onOpenHistory = { navController.navigate(ROUTE_HISTORY) },
                onOpenDownloads = { navController.navigate(ROUTE_DOWNLOADS) },
                onFindInPage = viewModel::openFindInPage,
                onTranslatePage = viewModel::translateCurrentPage,
                onToggleDesktopMode = viewModel::toggleDesktopMode,
                onOpenSettings = { navController.navigate(ROUTE_SETTINGS) }
            )
        }

        if (showPageInfo && !activeTab.isStartPage) {
            PageInfoDialog(
                tab = activeTab,
                onCopyUrl = { copyToClipboard(activeTab.url) },
                onDismiss = { showPageInfo = false }
            )
        }

        longPressHit?.let { hit ->
            LongPressContextSheet(
                hit = hit,
                onDismiss = { longPressHit = null },
                onOpenNewTab = { url -> viewModel.openNewTab(url = url, isIncognito = false) },
                onOpenIncognitoTab = { url -> viewModel.openNewTab(url = url, isIncognito = true) },
                onCopyLink = copyToClipboard,
                onShareLink = shareText
            )
        }

        // Fullscreen HTML5 Video Overlay
        customVideoView?.let { videoView ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        FrameLayout(ctx).apply {
                            (videoView.parent as? ViewGroup)?.removeView(videoView)
                            addView(videoView)
                        }
                    }
                )
            }
        }
    }
}
