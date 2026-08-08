package com.mattschoe.apptemplate

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.mattschoe.apptemplate.data.local.createDataStore
import com.mattschoe.apptemplate.data.local.getDatabaseBuilder
import com.mattschoe.apptemplate.data.repositories.OfflinePreferencesRepository

/**
 * The iOS composition root — mirrors `MyApplication.onCreate()`.
 * `remember` keeps the container alive across recompositions.
 */
fun MainViewController() = ComposeUIViewController {
    val appContainer = remember {
        val database = getDatabaseBuilder()
            .setDriver(BundledSQLiteDriver())
            .build()

        AppContainer(
            _database = database,
            preferencesRepository = OfflinePreferencesRepository(createDataStore())
        )
    }

    App(appContainer)
}
