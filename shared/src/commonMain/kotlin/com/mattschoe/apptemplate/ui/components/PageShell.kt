package com.mattschoe.apptemplate.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Every page's outermost composable. Owns the Scaffold and the app's standard
 * horizontal gutter so individual pages never re-invent their own padding —
 * change the numbers here and the whole app follows.
 */
@Composable
fun PageShell(
    modifier: Modifier = Modifier,
    floatingActionButton: (@Composable () -> Unit)? = null,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    pageContent: @Composable (PaddingValues) -> Unit,
) {
    Box(modifier = modifier) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = topBar,
            bottomBar = bottomBar,
            floatingActionButton = { if (floatingActionButton != null) floatingActionButton() },
        ) { innerPadding ->
            val padding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 12.dp,
                bottom = innerPadding.calculateBottomPadding(),
                start = 24.dp,
                end = 24.dp
            )
            pageContent(padding)
        }
    }
}
