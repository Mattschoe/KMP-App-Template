package com.mattschoe.apptemplate

import android.app.Application
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.mattschoe.apptemplate.data.local.createDataStore
import com.mattschoe.apptemplate.data.local.getDatabaseBuilder
import com.mattschoe.apptemplate.data.repositories.OfflinePreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * The Android composition root. Builds every platform-specific dependency, hands
 * them to [AppContainer], and exposes it for MainActivity to read.
 *
 * `appScope` outlives any screen — use it for work that must survive navigation
 * (initial sync, migrations), never for anything a ViewModel could own.
 */
class MyApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        val database = getDatabaseBuilder(applicationContext)
            .setDriver(BundledSQLiteDriver())
            .build()

        val preferencesRepository = OfflinePreferencesRepository(createDataStore(this))

        appContainer = AppContainer(
            _database = database,
            preferencesRepository = preferencesRepository
        )
    }
}
