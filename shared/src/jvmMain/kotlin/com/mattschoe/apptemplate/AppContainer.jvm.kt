package com.mattschoe.apptemplate

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.mattschoe.apptemplate.data.local.createDataStore
import com.mattschoe.apptemplate.data.local.getDatabaseBuilder
import com.mattschoe.apptemplate.data.repositories.OfflinePreferencesRepository

/**
 * The desktop composition root — the JVM twin of `MyApplication.onCreate()`.
 *
 * It lives in `shared` rather than in `desktopApp` so the app module stays a thin
 * window wrapper and never needs Room or SQLite on its own classpath.
 */
fun createAppContainer(): AppContainer {
    val database = getDatabaseBuilder()
        .setDriver(BundledSQLiteDriver())
        .build()

    return AppContainer(
        _database = database,
        preferencesRepository = OfflinePreferencesRepository(createDataStore())
    )
}
