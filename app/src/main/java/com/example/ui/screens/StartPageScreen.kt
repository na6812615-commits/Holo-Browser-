package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.ShortcutEntity
import com.example.model.SearchEngine
import com.example.model.UrlUtils
import com.example.ui.theme.HoloBluePrimary
import com.example.ui.theme.HoloCyanAccent
import com.example.ui.theme.HoloVioletAccent

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun StartPageScreen(
    isIncognito: Boolean,
    searchEngine: SearchEngine,
    shortcuts: List<ShortcutEntity>,
    onSearchBarClick: () -> Unit,
    onVoiceSearchClick: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onSearchTopic: (String) -> Unit,
    onSelectSearchEngine: (SearchEngine) -> Unit,
    onAddShortcut: (title: String, url: String) -> Unit,
    onDeleteShortcut: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddShortcutDialog by remember { mutableStateOf(false) }
    var shortcutToDelete by remember { mutableStateOf<ShortcutEntity?>(null) }
    var showEngineMenu by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isIncognito) {
                IncognitoStartContent(
                    searchEngine = searchEngine,
                    onSearchBarClick = onSearchBarClick,
                    onVoiceSearchClick = onVoiceSearchClick
                )
            } else {
                // Hero Holographic Banner Card + Overlapping Search Pill
                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(176.dp),
                        shape = RoundedCornerShape(28.dp),
                        tonalElevation = 4.dp
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = painterResource(id = R.drawable.img_holo_banner_1794992876455),
                                contentDescription = "Holo Browser Banner",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0x660B132B),
                                                Color(0xCC0B132B)
                                            )
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp, vertical = 18.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color.White.copy(alpha = 0.14f),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Shield,
                                                contentDescription = null,
                                                tint = HoloCyanAccent,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = "Holo Shield",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    // Search Engine Quick Switcher
                                    Box {
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .clickable { showEngineMenu = true }
                                                .testTag("start_engine_selector"),
                                            shape = RoundedCornerShape(20.dp),
                                            color = Color.White.copy(alpha = 0.16f),
                                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Public,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = searchEngine.displayName,
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.SemiBold
                                                    ),
                                                    color = Color.White
                                                )
                                            }
                                        }
                                        DropdownMenu(
                                            expanded = showEngineMenu,
                                            onDismissRequest = { showEngineMenu = false }
                                        ) {
                                            SearchEngine.entries.forEach { engine ->
                                                DropdownMenuItem(
                                                    text = { Text(engine.displayName) },
                                                    onClick = {
                                                        onSelectSearchEngine(engine)
                                                        showEngineMenu = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Column(
                                    modifier = Modifier.padding(bottom = 18.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = "Holo Browser",
                                        style = MaterialTheme.typography.displayLarge.copy(
                                            brush = Brush.horizontalGradient(
                                                listOf(Color.White, Color(0xFFBAE6FD), Color(0xFFDDD6FE))
                                            )
                                        )
                                    )
                                    Text(
                                        text = stringResource(R.string.holo_tagline),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }

                    // Overlapping Pill Search Box
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                            .align(Alignment.BottomCenter)
                            .offset(y = 26.dp)
                            .height(54.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .clickable(onClick = onSearchBarClick)
                            .testTag("start_search_box"),
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 6.dp,
                        border = BorderStroke(
                            1.dp,
                            Brush.horizontalGradient(
                                listOf(
                                    HoloCyanAccent.copy(alpha = 0.5f),
                                    HoloBluePrimary.copy(alpha = 0.4f),
                                    HoloVioletAccent.copy(alpha = 0.5f)
                                )
                            )
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.search_with_engine, searchEngine.displayName),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = onVoiceSearchClick,
                                modifier = Modifier.testTag("start_voice_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = stringResource(R.string.cd_voice_search),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(44.dp))

                // Quick Access Speed Dial Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.quick_access),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(
                        onClick = { showAddShortcutDialog = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.add_shortcut))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val columns = if (maxWidth > 520.dp) 6 else 4
                    val rows = (shortcuts + listOf<ShortcutEntity?>(null)).chunked(columns)

                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        rows.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowItems.forEach { shortcut ->
                                    if (shortcut != null) {
                                        ShortcutTile(
                                            shortcut = shortcut,
                                            onClick = { onOpenUrl(shortcut.url) },
                                            onLongClick = { shortcutToDelete = shortcut },
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else {
                                        AddShortcutTile(
                                            onClick = { showAddShortcutDialog = true },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                repeat(columns - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                // Discover Topics Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.discover_topics),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        val topics = listOf(
                            "Tin tức hôm nay",
                            "Thời tiết Hà Nội",
                            "Công nghệ AI mới nhất",
                            "Lập trình Android Kotlin",
                            "Giá vàng hôm nay",
                            "Bóng đá thế giới"
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            topics.forEach { topic ->
                                AssistChip(
                                    onClick = { onSearchTopic(topic) },
                                    label = { Text(topic, style = MaterialTheme.typography.labelLarge) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showAddShortcutDialog) {
        AddShortcutDialog(
            onDismiss = { showAddShortcutDialog = false },
            onAdd = { title, url ->
                onAddShortcut(title, url)
                showAddShortcutDialog = false
            }
        )
    }

    shortcutToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { shortcutToDelete = null },
            title = { Text("Xóa lối tắt") },
            text = { Text("Bạn muốn xóa lối tắt \"${item.title}\" khỏi trang chủ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteShortcut(item.id)
                        shortcutToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { shortcutToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ShortcutTile(
    shortcut: ShortcutEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 6.dp, horizontal = 2.dp)
            .testTag("shortcut_${shortcut.title}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(52.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(shortcut.accentHex).copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = shortcut.title.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(shortcut.accentHex)
                    )
                }
                AsyncImage(
                    model = UrlUtils.faviconUrlFor(shortcut.url),
                    contentDescription = shortcut.title,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = shortcut.title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AddShortcutTile(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 2.dp)
            .testTag("add_shortcut_tile"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(52.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_shortcut),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.add_shortcut),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun IncognitoStartContent(
    searchEngine: SearchEngine,
    onSearchBarClick: () -> Unit,
    onVoiceSearchClick: () -> Unit
) {
    Spacer(modifier = Modifier.height(28.dp))
    Box(
        modifier = Modifier
            .size(84.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.VisibilityOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(42.dp)
        )
    }
    Spacer(modifier = Modifier.height(20.dp))
    Text(
        text = stringResource(R.string.incognito_title),
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(10.dp))
    Text(
        text = stringResource(R.string.incognito_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 12.dp)
    )
    Spacer(modifier = Modifier.height(24.dp))
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .clickable(onClick = onSearchBarClick),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.search_with_engine, searchEngine.displayName),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onVoiceSearchClick) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = stringResource(R.string.cd_voice_search),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(24.dp))
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Holo Browser không lưu lại:",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "• Nhật ký duyệt web và lịch sử tìm kiếm của tab này\n• Dữ liệu bộ nhớ đệm và biểu mẫu khi đóng tab\n• Cookie phiên ẩn danh sau khi bạn đóng toàn bộ tab ẩn danh",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AddShortcutDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, url: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_shortcut_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tên trang web") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Địa chỉ URL (vd: github.com)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(title, url) },
                enabled = url.isNotBlank()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
