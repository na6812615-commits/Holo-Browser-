package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.BrowserTab
import com.example.model.FindInPageState
import com.example.model.SearchEngine
import com.example.model.SearchSuggestion
import com.example.model.SuggestionSource
import com.example.model.UrlUtils
import com.example.ui.theme.HoloBluePrimary
import com.example.ui.theme.HoloCyanAccent
import com.example.ui.theme.HoloVioletAccent
import com.example.ui.theme.SecureGreen
import com.example.ui.theme.WarningAmber

@Composable
fun OmniboxTopBar(
    activeTab: BrowserTab,
    tabCount: Int,
    searchEngine: SearchEngine,
    omniboxQuery: String,
    isOmniboxFocused: Boolean,
    findInPageState: FindInPageState,
    onOmniboxClick: () -> Unit,
    onOmniboxQueryChange: (String) -> Unit,
    onOmniboxSubmit: (String) -> Unit,
    onOmniboxClose: () -> Unit,
    onSecurityClick: () -> Unit,
    onReloadOrStopClick: () -> Unit,
    onVoiceSearchClick: () -> Unit,
    onTabsClick: () -> Unit,
    onMenuClick: () -> Unit,
    onFindQueryChange: (String) -> Unit,
    onFindNext: (Boolean) -> Unit,
    onCloseFind: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            if (findInPageState.isVisible) {
                FindInPageBar(
                    state = findInPageState,
                    onQueryChange = onFindQueryChange,
                    onFindNext = onFindNext,
                    onClose = onCloseFind
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isOmniboxFocused) {
                        IconButton(
                            onClick = onOmniboxClose,
                            modifier = Modifier.testTag("omnibox_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back)
                            )
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (activeTab.isIncognito) Icons.Default.VisibilityOff else Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    if (omniboxQuery.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.search_with_engine, searchEngine.displayName),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    BasicTextField(
                                        value = TextFieldValue(
                                            text = omniboxQuery,
                                            selection = TextRange(omniboxQuery.length)
                                        ),
                                        onValueChange = { onOmniboxQueryChange(it.text) },
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                                            color = MaterialTheme.colorScheme.onSurface
                                        ),
                                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                        keyboardOptions = KeyboardOptions(
                                            imeAction = ImeAction.Go,
                                            keyboardType = KeyboardType.Uri
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onGo = { onOmniboxSubmit(omniboxQuery) }
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(focusRequester)
                                            .testTag("omnibox_text_input")
                                    )
                                }
                                if (omniboxQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { onOmniboxQueryChange("") },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(R.string.cd_clear),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else {
                                    IconButton(
                                        onClick = onVoiceSearchClick,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = stringResource(R.string.cd_voice_search),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                        LaunchedEffect(isOmniboxFocused) {
                            focusRequester.requestFocus()
                        }
                    } else {
                        // Pill Omnibox Display Mode
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .clickable(onClick = onOmniboxClick)
                                .testTag("omnibox_pill"),
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp, end = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = if (activeTab.isStartPage) onOmniboxClick else onSecurityClick,
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    when {
                                        activeTab.isIncognito -> Icon(
                                            imageVector = Icons.Default.VisibilityOff,
                                            contentDescription = stringResource(R.string.incognito_badge),
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        activeTab.isStartPage -> Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        activeTab.isSecure -> Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = stringResource(R.string.cd_security),
                                            tint = SecureGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        else -> Icon(
                                            imageVector = Icons.Default.WarningAmber,
                                            contentDescription = stringResource(R.string.cd_security),
                                            tint = WarningAmber,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (activeTab.isStartPage) {
                                        stringResource(R.string.search_or_type_url)
                                    } else {
                                        UrlUtils.extractDomain(activeTab.url).ifBlank { activeTab.url }
                                    },
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (activeTab.isStartPage) FontWeight.Normal else FontWeight.Medium
                                    ),
                                    color = if (activeTab.isStartPage) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (!activeTab.isStartPage) {
                                    IconButton(
                                        onClick = onReloadOrStopClick,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("omnibox_reload_button")
                                    ) {
                                        Icon(
                                            imageVector = if (activeTab.isLoading) Icons.Default.Close else Icons.Default.Refresh,
                                            contentDescription = stringResource(
                                                if (activeTab.isLoading) R.string.cd_stop else R.string.cd_reload
                                            ),
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                } else {
                                    IconButton(
                                        onClick = onVoiceSearchClick,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("omnibox_voice_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = stringResource(R.string.cd_voice_search),
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Tab Counter Pill Button
                        Box(
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(onClick = onTabsClick)
                                .testTag("top_tabs_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(7.dp),
                                color = ColorTransparent(),
                                border = BorderStroke(
                                    1.8.dp,
                                    if (activeTab.isIncognito) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.size(23.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (tabCount > 99) "99+" else tabCount.toString(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = if (tabCount > 9) 9.5.sp else 11.sp
                                        ),
                                        color = if (activeTab.isIncognito) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = onMenuClick,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("top_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.cd_menu)
                            )
                        }
                    }
                }
            }

            // Holographic Gradient Progress Bar
            AnimatedVisibility(
                visible = activeTab.isLoading && activeTab.progress in 1..99,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val fraction = (activeTab.progress / 100f).coerceIn(0.05f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .height(3.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(HoloCyanAccent, HoloBluePrimary, HoloVioletAccent)
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorTransparent() = androidx.compose.ui.graphics.Color.Transparent

@Composable
private fun FindInPageBar(
    state: FindInPageState,
    onQueryChange: (String) -> Unit,
    onFindNext: (Boolean) -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(42.dp),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = state.query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("find_in_page_input"),
                    decorationBox = { inner ->
                        if (state.query.isEmpty()) {
                            Text(
                                text = stringResource(R.string.find_in_page),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        inner()
                    }
                )
                if (state.query.isNotEmpty()) {
                    Text(
                        text = "${state.activeMatchOrdinal}/${state.numberOfMatches}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        IconButton(onClick = { onFindNext(false) }) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Kết quả trước")
        }
        IconButton(onClick = { onFindNext(true) }) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Kết quả tiếp")
        }
        IconButton(onClick = onClose) {
            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_close))
        }
    }
}

@Composable
fun OmniboxSuggestionsOverlay(
    suggestions: List<SearchSuggestion>,
    onSuggestionClick: (SearchSuggestion) -> Unit,
    onInsertSuggestion: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onDismiss),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.96f)
    ) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(suggestions) { suggestion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSuggestionClick(suggestion) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = when (suggestion.source) {
                        SuggestionSource.BOOKMARK -> Icons.Default.Bookmark
                        SuggestionSource.HISTORY -> Icons.Default.History
                        SuggestionSource.DIRECT_URL -> Icons.Default.Language
                        SuggestionSource.WEB_SEARCH -> Icons.Default.Search
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (suggestion.source == SuggestionSource.BOOKMARK) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = suggestion.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!suggestion.url.isNullOrBlank()) {
                            Text(
                                text = suggestion.url,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (suggestion.source == SuggestionSource.WEB_SEARCH) {
                        IconButton(
                            onClick = { onInsertSuggestion(suggestion.text) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NorthWest,
                                contentDescription = "Điền từ khóa",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    modifier = Modifier.padding(start = 66.dp)
                )
            }
        }
    }
}
