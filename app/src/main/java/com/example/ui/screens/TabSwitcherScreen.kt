package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.BrowserTab
import com.example.model.UrlUtils
import com.example.ui.theme.HoloBluePrimary
import com.example.ui.theme.HoloCyanAccent
import com.example.ui.theme.IncognitoBackground
import com.example.ui.theme.IncognitoPrimary
import com.example.ui.theme.IncognitoSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabSwitcherScreen(
    tabs: List<BrowserTab>,
    activeTabId: String,
    initialIncognitoFilter: Boolean,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onNewTab: (Boolean) -> Unit,
    onCloseAllTabs: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var showIncognito by remember { mutableStateOf(initialIncognitoFilter) }
    val normalTabs = remember(tabs) { tabs.filter { !it.isIncognito } }
    val incognitoTabs = remember(tabs) { tabs.filter { it.isIncognito } }
    val displayedTabs = if (showIncognito) incognitoTabs else normalTabs

    val containerBg = if (showIncognito) IncognitoBackground else MaterialTheme.colorScheme.background

    Scaffold(
        containerColor = containerBg,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (showIncognito) IncognitoSurface else MaterialTheme.colorScheme.surface
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = if (showIncognito) IncognitoPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                title = {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = !showIncognito,
                            onClick = { showIncognito = false },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Tab,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        ) {
                            Text("${stringResource(R.string.normal_tabs)} (${normalTabs.size})")
                        }
                        SegmentedButton(
                            selected = showIncognito,
                            onClick = { showIncognito = true },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        ) {
                            Text("${stringResource(R.string.incognito_tabs)} (${incognitoTabs.size})")
                        }
                    }
                },
                actions = {
                    if (displayedTabs.isNotEmpty()) {
                        IconButton(
                            onClick = { onCloseAllTabs(showIncognito) },
                            modifier = Modifier.testTag("close_all_tabs_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = stringResource(R.string.close_all_tabs),
                                tint = if (showIncognito) IncognitoPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    onNewTab(showIncognito)
                    onBack()
                },
                containerColor = if (showIncognito) IncognitoPrimary else MaterialTheme.colorScheme.primary,
                contentColor = if (showIncognito) IncognitoBackground else MaterialTheme.colorScheme.onPrimary,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                },
                text = {
                    Text(
                        if (showIncognito) stringResource(R.string.new_incognito_tab)
                        else stringResource(R.string.new_tab)
                    )
                },
                modifier = Modifier.testTag("fab_new_tab")
            )
        }
    ) { innerPadding ->
        if (displayedTabs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = IncognitoPrimary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Chưa có tab ẩn danh nào",
                        style = MaterialTheme.typography.titleMedium,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Nhấn nút bên dưới để mở tab ẩn danh riêng tư.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val columns = when {
                    maxWidth > 720.dp -> 4
                    maxWidth > 500.dp -> 3
                    else -> 2
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    contentPadding = PaddingValues(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayedTabs, key = { it.id }) { tab ->
                        TabCard(
                            tab = tab,
                            isActive = tab.id == activeTabId,
                            onSelect = {
                                onSelectTab(tab.id)
                                onBack()
                            },
                            onClose = { onCloseTab(tab.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabCard(
    tab: BrowserTab,
    isActive: Boolean,
    onSelect: () -> Unit,
    onClose: () -> Unit
) {
    val cardBg = if (tab.isIncognito) IncognitoSurface else MaterialTheme.colorScheme.surface
    val border = if (isActive) {
        BorderStroke(2.dp, Brush.linearGradient(listOf(HoloCyanAccent, HoloBluePrimary)))
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(172.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onSelect)
            .testTag("tab_card_${tab.id}"),
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        border = border,
        tonalElevation = if (isActive) 4.dp else 1.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    )
                    .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (tab.isStartPage) {
                    Icon(
                        imageVector = if (tab.isIncognito) Icons.Default.VisibilityOff else Icons.Default.Public,
                        contentDescription = null,
                        tint = if (tab.isIncognito) IncognitoPrimary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    AsyncImage(
                        model = UrlUtils.faviconUrlFor(tab.url),
                        contentDescription = null,
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tab.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (tab.isIncognito) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cd_close),
                        tint = if (tab.isIncognito) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Tab preview body
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (tab.isIncognito) IncognitoPrimary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (tab.isStartPage) {
                            Icon(
                                imageVector = if (tab.isIncognito) Icons.Default.VisibilityOff else Icons.Default.Public,
                                contentDescription = null,
                                tint = if (tab.isIncognito) IncognitoPrimary else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            AsyncImage(
                                model = UrlUtils.faviconUrlFor(tab.url),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (tab.isStartPage) {
                            if (tab.isIncognito) "Trang chủ ẩn danh" else "Trang chủ Holo"
                        } else {
                            UrlUtils.extractDomain(tab.url).ifBlank { tab.url }
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = if (tab.isIncognito) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
