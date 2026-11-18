package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.BrowserSettings
import com.example.model.SearchEngine
import com.example.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "holo_settings")

class BrowserPreferences(private val context: Context) {
    private object Keys {
        val SEARCH_ENGINE = stringPreferencesKey("search_engine")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val JS_ENABLED = booleanPreferencesKey("js_enabled")
        val BLOCK_POPUPS = booleanPreferencesKey("block_popups")
        val DO_NOT_TRACK = booleanPreferencesKey("do_not_track")
        val HTTPS_ONLY = booleanPreferencesKey("https_only")
        val TEXT_ZOOM = intPreferencesKey("text_zoom")
    }

    val settingsFlow: Flow<BrowserSettings> = context.dataStore.data.map { prefs ->
        BrowserSettings(
            searchEngine = prefs[Keys.SEARCH_ENGINE]?.let {
                runCatching { SearchEngine.valueOf(it) }.getOrNull()
            } ?: SearchEngine.GOOGLE,
            themeMode = prefs[Keys.THEME_MODE]?.let {
                runCatching { ThemeMode.valueOf(it) }.getOrNull()
            } ?: ThemeMode.SYSTEM,
            javaScriptEnabled = prefs[Keys.JS_ENABLED] ?: true,
            blockPopups = prefs[Keys.BLOCK_POPUPS] ?: true,
            doNotTrack = prefs[Keys.DO_NOT_TRACK] ?: false,
            httpsOnly = prefs[Keys.HTTPS_ONLY] ?: false,
            defaultTextZoom = prefs[Keys.TEXT_ZOOM] ?: 100
        )
    }

    suspend fun setSearchEngine(engine: SearchEngine) {
        context.dataStore.edit { it[Keys.SEARCH_ENGINE] = engine.name }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setJavaScriptEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.JS_ENABLED] = enabled }
    }

    suspend fun setBlockPopups(blocked: Boolean) {
        context.dataStore.edit { it[Keys.BLOCK_POPUPS] = blocked }
    }

    suspend fun setDoNotTrack(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DO_NOT_TRACK] = enabled }
    }

    suspend fun setHttpsOnly(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HTTPS_ONLY] = enabled }
    }

    suspend fun setTextZoom(zoom: Int) {
        context.dataStore.edit { it[Keys.TEXT_ZOOM] = zoom.coerceIn(75, 150) }
    }
}
