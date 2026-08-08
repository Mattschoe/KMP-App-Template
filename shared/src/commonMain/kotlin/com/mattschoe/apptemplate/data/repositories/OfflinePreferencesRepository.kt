package com.mattschoe.apptemplate.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.mattschoe.apptemplate.domain.repositories.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore-backed [PreferencesRepository].
 *
 * `isDarkMode` is nullable on purpose: `null` means "follow the system", which is
 * distinct from an explicit light choice.
 */
class OfflinePreferencesRepository(
    private val _dataStore: DataStore<Preferences>
) : PreferencesRepository {

    override val isDarkMode: Flow<Boolean?> =
        _dataStore.data.map { prefs -> prefs[DARK_MODE] }

    override suspend fun setDarkMode(enabled: Boolean?) {
        _dataStore.edit { prefs ->
            if (enabled == null) prefs.remove(DARK_MODE) else prefs[DARK_MODE] = enabled
        }
    }

    private companion object {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
    }
}
