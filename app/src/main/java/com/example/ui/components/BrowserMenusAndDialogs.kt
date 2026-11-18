package com.example.ui.components

import android.webkit.WebView
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.BrowserTab
import com.example.model.WebHitContext
import com.example.ui.theme.SecureGreen
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserOverflowSheet(
    activeTab: BrowserTab,
    isBookmarked: Boolean,
    onDismiss: () -> Unit,
    onForward: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onReload: () -> Unit,
    onPageInfo: () -> Unit,
    onShare: () -> Unit,
    onNewTab: () -> Unit,
    onNewIncognitoTab: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenDownloads: () -> Unit,
    onFindInPage: () -> Unit,
    onTranslatePage: () -> Unit,
    onToggleDesktopMode: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // Quick Icon Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickActionIcon(
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    label = "Tiến",
                    enabled = activeTab.canGoForward,
                    onClick = { onDismiss(); onForward() }
                )
                QuickActionIcon(
                    icon = if (isBookmarked) Icons.Default.Star else Icons.Default.StarOutline,
                    label = "Dấu trang",
                    enabled = !activeTab.isStartPage,
                    onClick = { onDismiss(); onBookmarkToggle() }
                )
                QuickActionIcon(
                    icon = Icons.Default.Share,
                    label = "Chia sẻ",
                    enabled = !activeTab.isStartPage,
                    onClick = { onDismiss(); onShare() }
                )
                QuickActionIcon(
                    icon = Icons.Default.Info,
                    label = "Thông tin",
                    enabled = !activeTab.isStartPage,
                    onClick = { onDismiss(); onPageInfo() }
                )
                QuickActionIcon(
                    icon = Icons.Default.Refresh,
                    label = "Tải lại",
                    enabled = !activeTab.isStartPage,
                    onClick = { onDismiss(); onReload() }
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            )

            OverflowMenuItem(
                icon = Icons.Default.Add,
                title = stringResource(R.string.new_tab),
                tag = "menu_new_tab",
                onClick = { onDismiss(); onNewTab() }
            )
            OverflowMenuItem(
                icon = Icons.Default.VisibilityOff,
                title = stringResource(R.string.new_incognito_tab),
                tag = "menu_new_incognito_tab",
                onClick = { onDismiss(); onNewIncognitoTab() }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp, horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            OverflowMenuItem(
                icon = Icons.Default.Bookmarks,
                title = stringResource(R.string.bookmarks),
                tag = "menu_bookmarks",
                onClick = { onDismiss(); onOpenBookmarks() }
            )
            OverflowMenuItem(
                icon = Icons.Default.History,
                title = stringResource(R.string.history),
                tag = "menu_history",
                onClick = { onDismiss(); onOpenHistory() }
            )
            OverflowMenuItem(
                icon = Icons.Default.Download,
                title = stringResource(R.string.downloads),
                tag = "menu_downloads",
                onClick = { onDismiss(); onOpenDownloads() }
            )

            if (!activeTab.isStartPage) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
                OverflowMenuItem(
                    icon = Icons.Default.FindInPage,
                    title = stringResource(R.string.find_in_page),
                    tag = "menu_find_in_page",
                    onClick = { onDismiss(); onFindInPage() }
                )
                OverflowMenuItem(
                    icon = Icons.Default.Translate,
                    title = stringResource(R.string.translate_page),
                    tag = "menu_translate",
                    onClick = { onDismiss(); onTranslatePage() }
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDismiss(); onToggleDesktopMode() }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DesktopWindows,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.desktop_site),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = activeTab.isDesktopMode,
                        onCheckedChange = { onDismiss(); onToggleDesktopMode() }
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp, horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
            OverflowMenuItem(
                icon = Icons.Default.Settings,
                title = stringResource(R.string.settings),
                tag = "menu_settings",
                onClick = { onDismiss(); onOpenSettings() }
            )
        }
    }
}

@Composable
private fun QuickActionIcon(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = if (enabled) 1f else 0.4f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                },
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            }
        )
    }
}

@Composable
private fun OverflowMenuItem(
    icon: ImageVector,
    title: String,
    tag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 13.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun PageInfoDialog(
    tab: BrowserTab,
    onCopyUrl: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = if (tab.isSecure) Icons.Default.Lock else Icons.Default.WarningAmber,
                contentDescription = null,
                tint = if (tab.isSecure) SecureGreen else WarningAmber
            )
        },
        title = {
            Text(
                text = if (tab.isSecure) "Kết nối an toàn (HTTPS)" else "Kết nối không bảo mật",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = tab.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = tab.url,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onCopyUrl(); onDismiss() }) {
                Text("Sao chép URL")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LongPressContextSheet(
    hit: WebHitContext,
    onDismiss: () -> Unit,
    onOpenNewTab: (String) -> Unit,
    onOpenIncognitoTab: (String) -> Unit,
    onCopyLink: (String) -> Unit,
    onShareLink: (String) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = hit.extra,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            if (hit.type != WebView.HitTestResult.IMAGE_TYPE) {
                OverflowMenuItem(
                    icon = Icons.Default.OpenInNew,
                    title = "Mở trong tab mới",
                    tag = "ctx_open_new_tab",
                    onClick = { onDismiss(); onOpenNewTab(hit.extra) }
                )
                OverflowMenuItem(
                    icon = Icons.Default.VisibilityOff,
                    title = "Mở trong tab ẩn danh",
                    tag = "ctx_open_incognito",
                    onClick = { onDismiss(); onOpenIncognitoTab(hit.extra) }
                )
            }
            OverflowMenuItem(
                icon = Icons.Default.ContentCopy,
                title = stringResource(R.string.copy_link),
                tag = "ctx_copy_link",
                onClick = { onDismiss(); onCopyLink(hit.extra) }
            )
            OverflowMenuItem(
                icon = Icons.Default.Share,
                title = stringResource(R.string.share),
                tag = "ctx_share_link",
                onClick = { onDismiss(); onShareLink(hit.extra) }
            )
        }
    }
}
