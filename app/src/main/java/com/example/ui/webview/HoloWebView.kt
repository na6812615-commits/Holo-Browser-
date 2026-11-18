package com.example.ui.webview

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.BrowserSettings
import com.example.model.BrowserTab
import com.example.model.WebHitContext
import com.example.viewmodel.BrowserCommand
import kotlinx.coroutines.flow.SharedFlow

private const val DESKTOP_USER_AGENT =
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"

class WebViewPool {
    private val views = mutableMapOf<String, WebView>()
    private val defaultUserAgents = mutableMapOf<String, String>()

    fun getOrCreate(
        tabId: String,
        context: Context,
        creator: () -> WebView
    ): WebView {
        return views.getOrPut(tabId) {
            creator().also { webView ->
                defaultUserAgents[tabId] = webView.settings.userAgentString
            }
        }
    }

    fun get(tabId: String): WebView? = views[tabId]

    fun getDefaultUserAgent(tabId: String): String? = defaultUserAgents[tabId]

    fun removeAndDestroy(tabId: String, isIncognito: Boolean) {
        val webView = views.remove(tabId)
        defaultUserAgents.remove(tabId)
        if (webView != null) {
            (webView.parent as? ViewGroup)?.removeView(webView)
            if (isIncognito) {
                webView.clearCache(true)
                webView.clearHistory()
                webView.clearFormData()
            }
            webView.stopLoading()
            webView.destroy()
        }
    }

    fun destroyAll() {
        views.values.forEach { webView ->
            (webView.parent as? ViewGroup)?.removeView(webView)
            webView.stopLoading()
            webView.destroy()
        }
        views.clear()
        defaultUserAgents.clear()
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HoloWebViewContainer(
    activeTab: BrowserTab,
    settings: BrowserSettings,
    webViewPool: WebViewPool,
    commands: SharedFlow<BrowserCommand>,
    onProgressChanged: (String, Int) -> Unit,
    onTitleReceived: (String, String) -> Unit,
    onPageStarted: (String, String, Boolean, Boolean) -> Unit,
    onPageFinished: (String, String, String, Boolean, Boolean) -> Unit,
    onFindResult: (Int, Int) -> Unit,
    onDownloadRequested: (fileName: String, url: String, mimeType: String, dmId: Long) -> Unit,
    onFileChooserRequested: (ValueCallback<Array<Uri>>) -> Unit,
    onLongPressHit: (WebHitContext) -> Unit,
    onShowCustomView: (View?, WebChromeClient.CustomViewCallback?) -> Unit,
    onToastMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val latestSettings by rememberUpdatedState(settings)

    LaunchedEffect(commands) {
        commands.collect { cmd ->
            when (cmd) {
                is BrowserCommand.LoadUrl -> {
                    val wv = webViewPool.get(cmd.tabId)
                    if (wv != null) {
                        val headers = if (latestSettings.doNotTrack) {
                            mapOf("DNT" to "1")
                        } else {
                            emptyMap()
                        }
                        if (wv.url != cmd.url) {
                            wv.loadUrl(cmd.url, headers)
                        }
                    }
                }
                is BrowserCommand.Reload -> webViewPool.get(cmd.tabId)?.reload()
                is BrowserCommand.StopLoading -> webViewPool.get(cmd.tabId)?.stopLoading()
                is BrowserCommand.GoBack -> webViewPool.get(cmd.tabId)?.goBack()
                is BrowserCommand.GoForward -> webViewPool.get(cmd.tabId)?.goForward()
                is BrowserCommand.DestroyTab -> webViewPool.removeAndDestroy(cmd.tabId, cmd.isIncognito)
                is BrowserCommand.FindAll -> webViewPool.get(cmd.tabId)?.findAllAsync(cmd.query)
                is BrowserCommand.FindNext -> webViewPool.get(cmd.tabId)?.findNext(cmd.forward)
                is BrowserCommand.ClearFind -> webViewPool.get(cmd.tabId)?.clearMatches()
                is BrowserCommand.ClearWebData -> {
                    if (cmd.clearCookies) {
                        CookieManager.getInstance().removeAllCookies(null)
                        CookieManager.getInstance().flush()
                    }
                    if (cmd.clearCache) {
                        webViewPool.get(activeTab.id)?.clearCache(true)
                    }
                }
            }
        }
    }

    val containerFrame = remember { FrameLayout(context) }

    Box(modifier = modifier.fillMaxSize().background(Color.White)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { containerFrame },
            update = { frame ->
                val tabId = activeTab.id
                val webView = webViewPool.getOrCreate(tabId, context) {
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setFindListener { activeMatchOrdinal, numberOfMatches, isDoneCounting ->
                            if (isDoneCounting) {
                                onFindResult(activeMatchOrdinal, numberOfMatches)
                            }
                        }
                        setOnLongClickListener { view ->
                            val result = (view as WebView).hitTestResult
                            val extra = result.extra
                            if (!extra.isNullOrBlank() &&
                                (result.type == WebView.HitTestResult.SRC_ANCHOR_TYPE ||
                                    result.type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE ||
                                    result.type == WebView.HitTestResult.IMAGE_TYPE)
                            ) {
                                onLongPressHit(WebHitContext(type = result.type, extra = extra))
                                true
                            } else {
                                false
                            }
                        }
                        setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
                            runCatching {
                                val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
                                val request = DownloadManager.Request(Uri.parse(url)).apply {
                                    setMimeType(mimeType)
                                    val cookies = CookieManager.getInstance().getCookie(url)
                                    if (!cookies.isNullOrBlank()) addRequestHeader("cookie", cookies)
                                    addRequestHeader("User-Agent", userAgent)
                                    setTitle(fileName)
                                    setDescription("Đang tải xuống bằng Holo Browser")
                                    setNotificationVisibility(
                                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                                    )
                                    setDestinationInExternalPublicDir(
                                        Environment.DIRECTORY_DOWNLOADS,
                                        fileName
                                    )
                                }
                                val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                val dmId = dm.enqueue(request)
                                onDownloadRequested(fileName, url, mimeType ?: "*/*", dmId)
                            }.onFailure {
                                onToastMessage("Không thể tải tệp từ liên kết này")
                            }
                        }
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val reqUrl = request?.url?.toString() ?: return false
                                if (reqUrl.startsWith("http://") || reqUrl.startsWith("https://")) {
                                    if (latestSettings.httpsOnly && reqUrl.startsWith("http://")) {
                                        view?.loadUrl("https://" + reqUrl.removePrefix("http://"))
                                        return true
                                    }
                                    return false
                                }
                                return runCatching {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(reqUrl))
                                    context.startActivity(intent)
                                    true
                                }.getOrDefault(true)
                            }

                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                val u = url ?: return
                                onPageStarted(
                                    tabId,
                                    u,
                                    view?.canGoBack() ?: false,
                                    view?.canGoForward() ?: false
                                )
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                val u = url ?: return
                                onPageFinished(
                                    tabId,
                                    u,
                                    view?.title.orEmpty(),
                                    view?.canGoBack() ?: false,
                                    view?.canGoForward() ?: false
                                )
                            }
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                onProgressChanged(tabId, newProgress)
                            }

                            override fun onReceivedTitle(view: WebView?, title: String?) {
                                if (!title.isNullOrBlank()) {
                                    onTitleReceived(tabId, title)
                                }
                            }

                            override fun onShowFileChooser(
                                webView: WebView?,
                                filePathCallback: ValueCallback<Array<Uri>>?,
                                fileChooserParams: FileChooserParams?
                            ): Boolean {
                                if (filePathCallback != null) {
                                    onFileChooserRequested(filePathCallback)
                                    return true
                                }
                                return false
                            }

                            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                                onShowCustomView(view, callback)
                            }

                            override fun onHideCustomView() {
                                onShowCustomView(null, null)
                            }
                        }
                    }
                }

                // Apply latest settings & mode
                webView.settings.apply {
                    javaScriptEnabled = settings.javaScriptEnabled
                    domStorageEnabled = !activeTab.isIncognito
                    databaseEnabled = !activeTab.isIncognito
                    javaScriptCanOpenWindowsAutomatically = !settings.blockPopups
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    textZoom = settings.defaultTextZoom
                    mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    userAgentString = if (activeTab.isDesktopMode) {
                        DESKTOP_USER_AGENT
                    } else {
                        webViewPool.getDefaultUserAgent(tabId) ?: userAgentString
                    }
                }

                // Swap active WebView into container frame
                if (frame.childCount != 1 || frame.getChildAt(0) !== webView) {
                    frame.removeAllViews()
                    (webView.parent as? ViewGroup)?.removeView(webView)
                    frame.addView(webView)
                }

                // Load initial URL if WebView hasn't loaded anything yet
                if (activeTab.url.isNotBlank() && webView.url == null) {
                    val headers = if (settings.doNotTrack) mapOf("DNT" to "1") else emptyMap()
                    webView.loadUrl(activeTab.url, headers)
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            containerFrame.removeAllViews()
        }
    }
}
