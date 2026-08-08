package com.mattschoe.apptemplate

import com.mattschoe.apptemplate.data.local.AppDatabase
import com.mattschoe.apptemplate.data.repositories.OfflineItemRepository
import com.mattschoe.apptemplate.domain.repositories.ItemRepository
import com.mattschoe.apptemplate.domain.repositories.PreferencesRepository

/**
 * Manual dependency injection — no Hilt/Dagger/Koin anywhere in this project.
 *
 * Built once per platform in the platform entry point (`MyApplication.onCreate()`,
 * `MainViewController()`, `main()`) and threaded down through [App] into
 * `ApplicationNavigationHost`, which constructs each ViewModel from it.
 *
 * Convention: collaborators the container only needs internally take a leading
 * underscore and stay private; anything a ViewModel needs is a public `val`.
 * Expensive things go behind `by lazy` so app start stays cheap.
 */
class AppContainer(
    private val _database: AppDatabase,
    val preferencesRepository: PreferencesRepository
) {
    val itemRepository: ItemRepository by lazy {
        OfflineItemRepository(_database.itemDao())
    }
}
