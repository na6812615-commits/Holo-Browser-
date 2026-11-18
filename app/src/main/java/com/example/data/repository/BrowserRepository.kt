package com.example.data.repository

import android.net.Uri
import com.example.data.local.BookmarkDao
import com.example.data.local.BookmarkEntity
import com.example.data.local.DownloadDao
import com.example.data.local.DownloadEntity
import com.example.data.local.HistoryDao
import com.example.data.local.HistoryEntity
import com.example.data.local.ShortcutDao
import com.example.data.local.ShortcutEntity
import com.example.data.preferences.BrowserPreferences
import com.example.model.BrowserSettings
import com.example.model.SearchEngine
import com.example.model.SearchSuggestion
import com.example.model.SuggestionSource
import com.example.model.ThemeMode
import com.example.model.UrlUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.util.concurrent.TimeUnit

class BrowserRepository(
    private val bookmarkDao: BookmarkDao,
    private val historyDao: HistoryDao,
    private val shortcutDao: ShortcutDao,
    private val downloadDao: DownloadDao,
    private val preferences: BrowserPreferences
) {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .build()

    val bookmarks: Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()
    val history: Flow<List<HistoryEntity>> = historyDao.getAllHistory()
    val shortcuts: Flow<List<ShortcutEntity>> = shortcutDao.getAllShortcuts()
    val downloads: Flow<List<DownloadEntity>> = downloadDao.getAllDownloads()
    val settings: Flow<BrowserSettings> = preferences.settingsFlow

    suspend fun seedDefaultShortcutsIfEmpty() {
        if (shortcutDao.count() == 0) {
            shortcutDao.insertAll(
                listOf(
                    ShortcutEntity(title = "Google", url = "https://www.google.com", accentHex = 0xFF2563EB, sortOrder = 0),
                    ShortcutEntity(title = "YouTube", url = "https://m.youtube.com", accentHex = 0xFFDC2626, sortOrder = 1),
                    ShortcutEntity(title = "Wikipedia", url = "https://vi.m.wikipedia.org", accentHex = 0xFF475569, sortOrder = 2),
                    ShortcutEntity(title = "GitHub", url = "https://github.com", accentHex = 0xFF1E293B, sortOrder = 3),
                    ShortcutEntity(title = "VnExpress", url = "https://vnexpress.net", accentHex = 0xFF9F1239, sortOrder = 4),
                    ShortcutEntity(title = "Dịch", url = "https://translate.google.com", accentHex = 0xFF0284C7, sortOrder = 5),
                    ShortcutEntity(title = "Bản đồ", url = "https://maps.google.com", accentHex = 0xFF059669, sortOrder = 6),
                    ShortcutEntity(title = "Tin tức", url = "https://news.google.com", accentHex = 0xFF7C3AED, sortOrder = 7)
                )
            )
        }
    }

    suspend fun toggleBookmark(title: String, url: String): Boolean {
        val existing = bookmarkDao.getByUrl(url)
        return if (existing != null) {
            bookmarkDao.deleteByUrl(url)
            false
        } else {
            bookmarkDao.insertBookmark(
                BookmarkEntity(
                    title = title.ifBlank { UrlUtils.extractDomain(url).ifBlank { url } },
                    url = url
                )
            )
            true
        }
    }

    suspend fun saveBookmark(title: String, url: String, folder: String) {
        bookmarkDao.insertBookmark(
            BookmarkEntity(title = title.trim(), url = url.trim(), folder = folder)
        )
    }

    suspend fun updateBookmark(bookmark: BookmarkEntity) {
        bookmarkDao.updateBookmark(bookmark)
    }

    suspend fun deleteBookmark(id: Long) {
        bookmarkDao.deleteById(id)
    }

    suspend fun addHistory(title: String, url: String) {
        if (url.isBlank() || url.startsWith("about:")) return
        historyDao.insertHistory(
            HistoryEntity(
                title = title.ifBlank { UrlUtils.extractDomain(url).ifBlank { url } },
                url = url
            )
        )
    }

    suspend fun deleteHistoryItem(id: Long) {
        historyDao.deleteById(id)
    }

    suspend fun clearHistory() {
        historyDao.clearAll()
    }

    suspend fun addShortcut(title: String, url: String) {
        val colors = listOf(0xFF2563EB, 0xFF0891B2, 0xFF7C3AED, 0xFF059669, 0xFFD97706, 0xFFE11D48)
        val normalized = UrlUtils.normalizeInputToUrl(url, SearchEngine.GOOGLE)
        shortcutDao.insertShortcut(
            ShortcutEntity(
                title = title.trim().ifBlank { UrlUtils.extractDomain(normalized) },
                url = normalized,
                accentHex = colors.random(),
                sortOrder = 99
            )
        )
    }

    suspend fun deleteShortcut(id: Long) {
        shortcutDao.deleteById(id)
    }

    suspend fun recordDownload(
        fileName: String,
        url: String,
        mimeType: String,
        downloadManagerId: Long
    ): Long {
        return downloadDao.insertDownload(
            DownloadEntity(
                fileName = fileName,
                url = url,
                mimeType = mimeType,
                downloadManagerId = downloadManagerId,
                status = "Đã bắt đầu tải"
            )
        )
    }

    suspend fun updateDownloadStatus(id: Long, status: String) {
        downloadDao.updateStatus(id, status)
    }

    suspend fun deleteDownload(id: Long) {
        downloadDao.deleteById(id)
    }

    suspend fun clearDownloads() {
        downloadDao.clearAll()
    }

    suspend fun setSearchEngine(engine: SearchEngine) = preferences.setSearchEngine(engine)
    suspend fun setThemeMode(mode: ThemeMode) = preferences.setThemeMode(mode)
    suspend fun setJavaScriptEnabled(enabled: Boolean) = preferences.setJavaScriptEnabled(enabled)
    suspend fun setBlockPopups(blocked: Boolean) = preferences.setBlockPopups(blocked)
    suspend fun setDoNotTrack(enabled: Boolean) = preferences.setDoNotTrack(enabled)
    suspend fun setHttpsOnly(enabled: Boolean) = preferences.setHttpsOnly(enabled)
    suspend fun setTextZoom(zoom: Int) = preferences.setTextZoom(zoom)

    suspend fun getSuggestions(query: String): List<SearchSuggestion> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return@withContext emptyList()

        val results = mutableListOf<SearchSuggestion>()
        val seen = mutableSetOf<String>()

        if (UrlUtils.isProbablyUrl(trimmed)) {
            val normalized = UrlUtils.normalizeInputToUrl(trimmed, SearchEngine.GOOGLE)
            results.add(
                SearchSuggestion(
                    text = normalized,
                    url = normalized,
                    source = SuggestionSource.DIRECT_URL
                )
            )
            seen.add(normalized.lowercase())
        }

        val bookmarkMatches = bookmarkDao.searchBookmarks(trimmed)
        bookmarkMatches.forEach { b ->
            if (seen.add(b.url.lowercase())) {
                results.add(
                    SearchSuggestion(
                        text = b.title,
                        url = b.url,
                        source = SuggestionSource.BOOKMARK
                    )
                )
            }
        }

        val historyMatches = historyDao.searchHistory(trimmed)
        historyMatches.forEach { h ->
            if (seen.add(h.url.lowercase())) {
                results.add(
                    SearchSuggestion(
                        text = h.title,
                        url = h.url,
                        source = SuggestionSource.HISTORY
                    )
                )
            }
        }

        // Fetch live Google autocomplete suggestions
        runCatching {
            val reqUrl = "https://suggestqueries.google.com/complete/search?client=firefox&hl=vi&q=${Uri.encode(trimmed)}"
            val request = Request.Builder().url(reqUrl).build()
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val root = JSONArray(body)
                    if (root.length() > 1) {
                        val suggestionsArray = root.getJSONArray(1)
                        val limit = minOf(suggestionsArray.length(), 6)
                        for (i in 0 until limit) {
                            val item = suggestionsArray.getString(i)
                            if (seen.add(item.lowercase())) {
                                results.add(
                                    SearchSuggestion(
                                        text = item,
                                        url = null,
                                        source = SuggestionSource.WEB_SEARCH
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        if (results.none { it.source == SuggestionSource.WEB_SEARCH && it.text.equals(trimmed, ignoreCase = true) }) {
            results.add(
                0,
                SearchSuggestion(
                    text = trimmed,
                    url = null,
                    source = SuggestionSource.WEB_SEARCH
                )
            )
        }

        results.take(9)
    }
}
