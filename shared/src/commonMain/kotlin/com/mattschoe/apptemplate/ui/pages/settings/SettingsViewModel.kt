package com.mattschoe.apptemplate.ui.pages.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mattschoe.apptemplate.domain.repositories.PreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val _preferencesRepository: PreferencesRepository
) : ViewModel() {

    /** `null` = follow the system. */
    val isDarkMode: StateFlow<Boolean?> = _preferencesRepository.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun setDarkMode(enabled: Boolean?) {
        viewModelScope.launch { _preferencesRepository.setDarkMode(enabled) }
    }
}
