package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.BrowserTab

@Composable
fun BrowserBottomBar(
    activeTab: BrowserTab,
    isCurrentBookmarked: Boolean,
    onBackClick: () -> Unit,
    onForwardClick: () -> Unit,
    onHomeClick: () -> Unit,
    onBookmarkToggleClick: () -> Unit,
    onBookmarksScreenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(54.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                enabled = activeTab.canGoBack || !activeTab.isStartPage,
                modifier = Modifier.testTag("bottom_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back)
                )
            }

            IconButton(
                onClick = onForwardClick,
                enabled = activeTab.canGoForward,
                modifier = Modifier.testTag("bottom_forward_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = stringResource(R.string.cd_forward)
                )
            }

            IconButton(
                onClick = onHomeClick,
                modifier = Modifier.testTag("bottom_home_button")
            ) {
                Icon(
                    imageVector = if (activeTab.isStartPage) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = stringResource(R.string.cd_home),
                    tint = if (activeTab.isStartPage) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            IconButton(
                onClick = {
                    if (activeTab.isStartPage) onBookmarksScreenClick() else onBookmarkToggleClick()
                },
                modifier = Modifier.testTag("bottom_bookmark_button")
            ) {
                Icon(
                    imageVector = when {
                        activeTab.isStartPage -> Icons.Outlined.Bookmarks
                        isCurrentBookmarked -> Icons.Filled.Star
                        else -> Icons.Outlined.StarOutline
                    },
                    contentDescription = stringResource(R.string.cd_bookmark),
                    tint = if (isCurrentBookmarked && !activeTab.isStartPage) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(
                onClick = onBookmarksScreenClick,
                modifier = Modifier.testTag("bottom_bookmarks_library_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Bookmarks,
                    contentDescription = stringResource(R.string.bookmarks)
                )
            }
        }
    }
}
