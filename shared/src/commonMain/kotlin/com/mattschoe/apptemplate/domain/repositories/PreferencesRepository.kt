package com.mattschoe.apptemplate.domain.repositories

import kotlinx.coroutines.flow.Flow

/**
 * User settings. Reads are streams so the UI reacts without polling; writes are
 * suspend and fire-and-forget (DataStore already retries and persists atomically).
 */
interface PreferencesRepository {
    val isDarkMode: Flow<Boolean?>
    suspend fun setDarkMode(enabled: Boolean?)
}
