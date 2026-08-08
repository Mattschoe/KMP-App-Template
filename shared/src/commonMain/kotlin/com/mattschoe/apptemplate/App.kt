package com.mattschoe.apptemplate

import androidx.compose.runtime.Composable
import com.mattschoe.apptemplate.ui.navigation.ApplicationNavigationHost
import com.mattschoe.apptemplate.ui.theme.AppTheme

/**
 * Single composable root, shared by all three platform entry points. Everything
 * platform-specific has already happened by the time this is called.
 */
@Composable
fun App(appContainer: AppContainer) {
    AppTheme(preferencesRepository = appContainer.preferencesRepository) {
        ApplicationNavigationHost(appContainer)
    }
}
