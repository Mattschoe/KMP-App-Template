package com.mattschoe.apptemplate.ui.pages.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mattschoe.apptemplate.domain.DataError
import com.mattschoe.apptemplate.domain.Item
import com.mattschoe.apptemplate.domain.Result
import com.mattschoe.apptemplate.domain.repositories.ItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = true
)

/**
 * ViewModels take their dependencies as constructor parameters — the nav host
 * builds them from the AppContainer. They never reach for a container themselves.
 *
 * Repository streams are converted with `stateIn(WhileSubscribed(5000))` so the
 * underlying Room query is torn down 5s after the last collector goes away, which
 * survives configuration changes without leaking.
 */
class HomeViewModel(
    private val _itemRepository: ItemRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = _itemRepository.observeItems()
        .map { items -> HomeUiState(items = items, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    /** Set when a write fails; the page shows it and calls [consumeError]. */
    private val _errorState = MutableStateFlow<DataError?>(null)
    val errorState: StateFlow<DataError?> = _errorState.asStateFlow()

    fun addItem(name: String) {
        viewModelScope.launch {
            when (val result = _itemRepository.addItem(name)) {
                is Result.Success -> Unit
                is Result.Error -> _errorState.value = result.error
            }
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            when (val result = _itemRepository.deleteItem(id)) {
                is Result.Success -> Unit
                is Result.Error -> _errorState.value = result.error
            }
        }
    }

    fun consumeError() {
        _errorState.value = null
    }
}
