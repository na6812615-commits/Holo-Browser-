package com.example.model

import android.webkit.WebView
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID

enum class SearchEngine(
    val displayName: String,
    val searchUrlTemplate: String,
    val homeUrl: String
) {
    GOOGLE(
        displayName = "Google",
        searchUrlTemplate = "https://www.google.com/search?q=%s",
        homeUrl = "https://www.google.com"
    ),
    DUCKDUCKGO(
        displayName = "DuckDuckGo",
        searchUrlTemplate = "https://duckduckgo.com/?q=%s",
        homeUrl = "https://duckduckgo.com"
    ),
    BING(
        displayName = "Bing",
        searchUrlTemplate = "https://www.bing.com/search?q=%s",
        homeUrl = "https://www.bing.com"
    ),
    ECOSIA(
        displayName = "Ecosia",
        searchUrlTemplate = "https://www.ecosia.org/search?q=%s",
        homeUrl = "https://www.ecosia.org"
    )
}

enum class ThemeMode(val label: String) {
    SYSTEM("Theo hệ thống"),
    LIGHT("Giao diện sáng"),
    DARK("Giao diện tối")
}

data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Tab mới",
    val url: String = "",
    val isIncognito: Boolean = false,
    val isDesktopMode: Boolean = false,
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isSecure: Boolean = true
) {
    val isStartPage: Boolean get() = url.isBlank()
}

enum class SuggestionSource {
    DIRECT_URL,
    BOOKMARK,
    HISTORY,
    WEB_SEARCH
}

data class SearchSuggestion(
    val text: String,
    val url: String? = null,
    val source: SuggestionSource
)

data class FindInPageState(
    val isVisible: Boolean = false,
    val query: String = "",
    val activeMatchOrdinal: Int = 0,
    val numberOfMatches: Int = 0
)

data class WebHitContext(
    val type: Int = WebView.HitTestResult.UNKNOWN_TYPE,
    val extra: String = ""
)

data class BrowserSettings(
    val searchEngine: SearchEngine = SearchEngine.GOOGLE,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val javaScriptEnabled: Boolean = true,
    val blockPopups: Boolean = true,
    val doNotTrack: Boolean = false,
    val httpsOnly: Boolean = false,
    val defaultTextZoom: Int = 100
)

object UrlUtils {
    private val domainRegex = Regex(
        "^(https?://)?([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(:\\d+)?(/.*)?$"
    )
    private val ipRegex = Regex(
        "^(https?://)?(\\d{1,3}\\.){3}\\d{1,3}(:\\d+)?(/.*)?$"
    )
    private val localhostRegex = Regex("^(https?://)?localhost(:\\d+)?(/.*)?$")

    fun isProbablyUrl(raw: String): Boolean {
        val trimmed = raw.trim()
        if (trimmed.isEmpty() || trimmed.contains(" ")) return false
        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("about:", ignoreCase = true)
        ) {
            return true
        }
        return domainRegex.matches(trimmed) ||
            ipRegex.matches(trimmed) ||
            localhostRegex.matches(trimmed)
    }

    fun normalizeInputToUrl(
        raw: String,
        engine: SearchEngine,
        httpsOnly: Boolean = false
    ): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return ""
        if (trimmed.startsWith("http://", ignoreCase = true)) {
            return if (httpsOnly) "https://" + trimmed.removePrefix("http://") else trimmed
        }
        if (trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("about:", ignoreCase = true)
        ) {
            return trimmed
        }
        if (isProbablyUrl(trimmed)) {
            val scheme = if (trimmed.startsWith("localhost") || ipRegex.matches(trimmed)) {
                "http://"
            } else {
                "https://"
            }
            return "$scheme$trimmed"
        }
        val encoded = URLEncoder.encode(trimmed, StandardCharsets.UTF_8.name())
        return engine.searchUrlTemplate.replace("%s", encoded)
    }

    fun extractDomain(url: String): String {
        if (url.isBlank()) return ""
        return try {
            val host = URI(url).host.orEmpty()
            host.removePrefix("www.")
        } catch (_: Exception) {
            url
        }
    }

    fun faviconUrlFor(url: String): String {
        val domain = extractDomain(url)
        return if (domain.isNotBlank()) {
            "https://www.google.com/s2/favicons?domain=${URLEncoder.encode(domain, StandardCharsets.UTF_8.name())}&sz=64"
        } else {
            ""
        }
    }

    fun translatePageUrl(url: String): String {
        val encoded = URLEncoder.encode(url, StandardCharsets.UTF_8.name())
        return "https://translate.google.com/translate?sl=auto&tl=vi&u=$encoded"
    }
}
